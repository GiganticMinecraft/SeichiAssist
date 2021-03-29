package com.github.unchama.seichiassist.subsystems.gachapoint

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, IO, SyncEffect, Timer}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, OnMinecraftServerThread}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.application.process.AddSeichiExpAsGachaPoint
import com.github.unchama.seichiassist.subsystems.gachapoint.application.repository.GachaPointRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.gachapoint.bukkit.GrantBukkitGachaTicketToAPlayer
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.GrantGachaTicketToAPlayer
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import com.github.unchama.seichiassist.subsystems.gachapoint.infrastructure.JdbcGachaPointPersistence
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

trait System[F[_], G[_], Player] extends Subsystem[F] {

  val api: GachaPointApi[F, G, Player]

}

object System {

  import cats.effect.implicits._
  import cats.implicits._

  def wired[
    F[_] : ConcurrentEffect : Timer : GetConnectedPlayers[*[_], Player] : ErrorLogger,
    G[_] : SyncEffect
  ](breakCountReadAPI: BreakCountReadAPI[F, G, Player])
   (implicit ioOnMainThread: OnMinecraftServerThread[IO]): G[System[F, G, Player]] = {
    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance

    val gachaPointPersistence = new JdbcGachaPointPersistence[G]

    val grantEffectFactory: Player => GrantGachaTicketToAPlayer[F] =
      player => GrantBukkitGachaTicketToAPlayer[F](player)

    for {
      gachaPointRepositoryControls <-
        BukkitRepositoryControls.createHandles(
          GachaPointRepositoryDefinition.withContext[G, F, Player](gachaPointPersistence)(grantEffectFactory)
        )

      _ <- {
        val gachaPointRepository =
          gachaPointRepositoryControls.repository.map(_.pointRef.mapK[F](ContextCoercion.asFunctionK))

        val streams: List[fs2.Stream[F, Unit]] = List(
          AddSeichiExpAsGachaPoint.stream(gachaPointRepository)(breakCountReadAPI.seichiAmountIncreases),
        )

        EffectExtra.runAsyncAndForget[F, G, Unit] {
          streams
            .traverse(StreamExtra.compileToRestartingStream(_).start)
            .as(())
        }
      }
    } yield {
      new System[F, G, Player] {
        override val api: GachaPointApi[F, G, Player] = new GachaPointApi[F, G, Player] {
          override val gachaPoint: KeyedDataRepository[Player, ReadOnlyRef[G, GachaPoint]] =
            gachaPointRepositoryControls
              .repository
              .map(value => ReadOnlyRef.fromRef(value.pointRef))

          override val receiveBatch: Kleisli[F, Player, Unit] = Kleisli { player =>
            gachaPointRepositoryControls
              .repository
              .lift(player)
              .traverse { value =>
                value.semaphore.tryBatchTransaction
              }
              .as(())
          }

          override def addGachaPoint(point: GachaPoint): Kleisli[G, Player, Unit] = Kleisli { player =>
            gachaPointRepositoryControls
              .repository
              .lift(player)
              .traverse { value =>
                value.pointRef.update(_.add(point))
              }
              .as(())
          }
        }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          gachaPointRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }
}
