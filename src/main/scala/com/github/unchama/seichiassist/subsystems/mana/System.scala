package com.github.unchama.seichiassist.subsystems.mana

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.mana.application.ManaRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.mana.application.process.{
  RefillToCap,
  UpdateManaCaps
}
import com.github.unchama.seichiassist.subsystems.mana.domain.{
  LevelCappedManaAmount,
  ManaAmountPersistence,
  ManaManipulation,
  ManaMultiplier
}
import com.github.unchama.seichiassist.subsystems.mana.infrastructure.JdbcManaAmountPersistence
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

trait System[F[_], G[_], Player] extends Subsystem[F] {

  val manaApi: ManaApi[F, G, Player]

}

object System {

  import cats.effect.implicits._
  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: ErrorLogger, G[_]: SyncEffect: ContextCoercion[*[_], F]](
    implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]
  ): F[System[F, G, Player]] = {
    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance

    val manaPersistence: ManaAmountPersistence[G] = new JdbcManaAmountPersistence[G]

    for {
      topic <- Fs3Topic[F, Option[(Player, LevelCappedManaAmount)]]
      globalMultiplierRef <- Ref.in[F, G, ManaMultiplier](ManaMultiplier(1))
      handles <- ContextCoercion {
        BukkitRepositoryControls.createHandles(
          ManaRepositoryDefinition.withContext[F, G, Player](
            stream => stream.map(Some.apply).through(topic.publish),
            manaPersistence
          )
        )
      }
      _ <- List(
        UpdateManaCaps.using[F, G, Player](handles.repository),
        RefillToCap.using[F, G, Player](handles.repository)
      ).traverse(StreamExtra.compileToRestartingStream[F, Unit]("[Mana]")(_).start)
    } yield new System[F, G, Player] {
      override val manaApi: ManaApi[F, G, Player] = new ManaApi[F, G, Player] {
        override val readManaAmount: KeyedDataRepository[Player, G[LevelCappedManaAmount]] =
          handles.repository.map(_.get)

        override val manaAmountUpdates: fs2.Stream[F, (Player, LevelCappedManaAmount)] =
          topic.subscribe(1).mapFilter(identity)

        override val manaAmount: KeyedDataRepository[Player, ManaManipulation[G]] =
          handles
            .repository
            .map(ManaManipulation.fromLevelCappedAmountRef[G](globalMultiplierRef))

        override def setManaConsumingMultiplier(manaMultiplier: ManaMultiplier): G[Unit] =
          globalMultiplierRef.set(manaMultiplier)

      }

      override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = List(
        handles.coerceFinalizationContextTo[F]
      )
    }
  }
}
