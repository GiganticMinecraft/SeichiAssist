package com.github.unchama.seichiassist.subsystems.gachapoint

import cats.data.Kleisli
import cats.effect.{Async, ConcurrentEffect, Sync, SyncEffect, Timer}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.minecraft.actions.GetConnectedPlayers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.application.process.{AddSeichiExpAsGachaPoint, ConvertPointToTickets}
import com.github.unchama.seichiassist.subsystems.gachapoint.application.repository.{GachaPointRepositoryDefinitions, GachaTicketReceivingSettingsRepositoryDefinitions}
import com.github.unchama.seichiassist.subsystems.gachapoint.bukkit.GrantBukkitGachaTicketToAPlayer
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.GrantGachaTicketToAPlayer
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings.GachaTicketReceivingSettings
import com.github.unchama.seichiassist.subsystems.gachapoint.infrastructure.{JdbcGachaPointPersistence, JdbcGachaTicketReceivingSettingsPersistence}
import org.bukkit.entity.Player

trait System[F[_], G[_], Player] extends Subsystem[F] {

  val settingsApi: GachaPointSettingsApi[G, Player]

  val api: GachaPointApi[F, G, Player]

}

object System {

  import cats.effect.implicits._
  import cats.implicits._

  def backgroundProcess[
    F[_] : Async, G[_]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    breakCountReadAPI
      .seichiAmountIncreases
      .evalTap { case (player, amount) =>
        Sync[F].delay {
          // TODO: gachapointのリポジトリをこのsubsystemで持ってplayermapを参照しないようにする
          SeichiAssist.playermap.get(player.getUniqueId).foreach(_.gachapoint += amount.amount.toInt)
        }
      }
      .compile.drain
      .flatMap[Nothing](_ => Async[F].never)
  }

  def wired[
    F[_] : ConcurrentEffect : Timer : GetConnectedPlayers[*[_], Player],
    G[_] : SyncEffect
  ](breakCountReadAPI: BreakCountReadAPI[F, G, Player]): G[System[F, G, Player]] = {
    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance

    val gachaPointPersistence = new JdbcGachaPointPersistence[G]
    val gachaTicketReceivingSettingsPersistence = new JdbcGachaTicketReceivingSettingsPersistence[G]

    val grantEffectFactory: Player => GrantGachaTicketToAPlayer[F] =
      player => GrantBukkitGachaTicketToAPlayer[F](player)

    for {
      gachaPointRepositoryControls <-
        BukkitRepositoryControls.createTwoPhasedRepositoryAndHandles(
          GachaPointRepositoryDefinitions.initialization[G, F, Player](gachaPointPersistence)(grantEffectFactory),
          GachaPointRepositoryDefinitions.finalization[G, F, Player](gachaPointPersistence)
        )
      gachaTicketReceivingSettingsRepositoryControls <-
        BukkitRepositoryControls.createSinglePhasedRepositoryAndHandles(
          GachaTicketReceivingSettingsRepositoryDefinitions.initialization(gachaTicketReceivingSettingsPersistence),
          GachaTicketReceivingSettingsRepositoryDefinitions.finalization(gachaTicketReceivingSettingsPersistence)
        )

      _ <- {
        val gachaPointRepository =
          gachaPointRepositoryControls.repository.map(_.pointRef.mapK[F](ContextCoercion.asFunctionK))

        val semaphoreRepository =
          gachaPointRepositoryControls.repository.map(_.semaphore)

        val settingsRepository =
          gachaTicketReceivingSettingsRepositoryControls
            .repository.map(_.mapK[F](ContextCoercion.asFunctionK))

        EffectExtra.runAsyncAndForget[F, G, Unit] {
          List(
            AddSeichiExpAsGachaPoint.stream(gachaPointRepository)(breakCountReadAPI.seichiAmountIncreases),
            ConvertPointToTickets.stream(settingsRepository, semaphoreRepository)
          ).traverse(_.compile.drain.start).as(())
        }
      }
    } yield {
      new System[F, G, Player] {
        override val settingsApi: GachaPointSettingsApi[G, Player] = new GachaPointSettingsApi[G, Player] {
          override val ticketReceivingSettings: KeyedDataRepository[Player, ReadOnlyRef[G, GachaTicketReceivingSettings]] =
            gachaTicketReceivingSettingsRepositoryControls
              .repository
              .map(ref => ReadOnlyRef.fromRef(ref))

          override val toggleTicketReceivingSettings: Kleisli[G, Player, Unit] = Kleisli { player =>
            gachaTicketReceivingSettingsRepositoryControls
              .repository
              .lift(player)
              .traverse(_.update(_.next))
              .as(())
          }
        }

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
          gachaPointRepositoryControls.coerceFinalizationContextTo[F],
          gachaTicketReceivingSettingsRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }
}
