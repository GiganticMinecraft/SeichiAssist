package com.github.unchama.seichiassist.subsystems.managedfly.bukkit

import java.util.UUID

import cats.effect.{ConcurrentEffect, IO, SyncEffect, Timer}
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.managedfly.application.{PlayerFlySessionGateway, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import org.bukkit.entity.Player

class BukkitFlySessionGatewayRepository[
  AsyncContext[_] : ConcurrentEffect : MinecraftServerThreadShift : Timer,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
](implicit effectEnvironment: EffectEnvironment, configuration: SystemConfiguration)
  extends TwoPhasedPlayerDataRepository[AsyncContext, SyncContext, PlayerFlySessionGateway[AsyncContext, SyncContext]] {

  override protected type TemporaryData = Option[RemainingFlyDuration]

  // TODO DBに永続化した値を読み込む
  override protected val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], Option[RemainingFlyDuration]]] = {
    (_, _) => SyncEffect[SyncContext].pure(Right(None))
  }

  override protected def initializeValue(player: Player,
                                         temporaryData: Option[RemainingFlyDuration]
                                        ): SyncContext[PlayerFlySessionGateway[AsyncContext, SyncContext]] = {
    val factory: UuidBasedPlayerFlySessionFactory[AsyncContext] =
      new UuidBasedPlayerFlySessionFactory[AsyncContext](player.getUniqueId, configuration.expConsumptionAmount)

    PlayerFlySessionGateway
      .createNew[AsyncContext, SyncContext](factory)
      .flatTap { gateway =>
        temporaryData match {
          case Some(duration) => gateway.startNewSessionAndForget(duration)
          case None => SyncEffect[SyncContext].unit
        }
      }
  }

  // TODO DBに永続化する値を書き込む
  override protected val unloadData: (Player, PlayerFlySessionGateway[AsyncContext, SyncContext]) => SyncContext[Unit] = {
    (_, sessionRef) =>
      sessionRef.stopAnyRunningSession
        .runAsync(_ => IO.unit)
        .runSync[SyncContext]
  }
}
