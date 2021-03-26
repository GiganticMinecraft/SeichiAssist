package com.github.unchama.seichiassist.subsystems.mana

import cats.effect.{Async, ConcurrentEffect, Sync, SyncEffect}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.fs2.workaround.Topic
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.generic.{ContextCoercion, Diff}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.mana.application.ManaRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.mana.domain.{LevelCappedManaAmount, ManaAmountPersistence, ManaManipulation}
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

trait System[F[_], G[_], Player] extends Subsystem[G] {

  val manaApi: ManaApi[F, G, Player]

}

object System {

  import cats.implicits._

  def wired[
    F[_] : ConcurrentEffect : ErrorLogger,
    G[_] : SyncEffect : ContextCoercion[*[_], F]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[System[F, G, Player]] = {
    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance

    val manaPersistence: ManaAmountPersistence[G] = ???

    for {
      topic <- Topic[F, Option[(Player, LevelCappedManaAmount)]](None)
      handles <- ContextCoercion {
        BukkitRepositoryControls.createHandles(
          ManaRepositoryDefinition.withContext[F, G, Player](
            stream => stream.map(Some.apply).through(topic.publish),
            manaPersistence
          )
        )
      }
    } yield new System[F, G, Player] {
      override val manaApi: ManaApi[F, G, Player] = new ManaApi[F, G, Player] {
        override val readManaAmount: KeyedDataRepository[Player, G[LevelCappedManaAmount]] =
          handles.repository.map(_.get)

        override val manaAmountUpdates: fs2.Stream[F, (Player, LevelCappedManaAmount)] =
          topic.subscribe(1).mapFilter(identity)

        override val manaAmount: KeyedDataRepository[Player, ManaManipulation[G]] =
          handles.repository.map(ManaManipulation.fromLevelCappedAmountRef[G])
      }

      override val managedRepositoryControls: Seq[BukkitRepositoryControls[G, _]] = List(handles)
    }
  }

  def backgroundProcess[
    F[_] : Async : ErrorLogger, G[_]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    StreamExtra.compileToRestartingStream {
      breakCountReadAPI
        .seichiLevelUpdates
        .evalTap { case (player, Diff(_, newLevel)) =>
          Sync[F].delay {
            // TODO: manaのリポジトリをこのsubsystemで持ってplayermapを参照しないようにする
            SeichiAssist.playermap.get(player.getUniqueId).foreach(_.manaState.onLevelUp(player, newLevel))
          }
        }
    }
  }

}
