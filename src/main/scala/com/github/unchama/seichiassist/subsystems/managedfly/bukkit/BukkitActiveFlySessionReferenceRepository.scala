package com.github.unchama.seichiassist.subsystems.managedfly.bukkit

import java.util.UUID

import cats.effect.{ConcurrentEffect, IO, SyncEffect, Timer}
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.managedfly.application.{ActiveSessionFactory, ActiveSessionReference, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import org.bukkit.entity.Player

class BukkitActiveFlySessionReferenceRepository[
  AsyncContext[_] : ConcurrentEffect : MinecraftServerThreadShift : Timer,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
](implicit effectEnvironment: EffectEnvironment,
  configuration: SystemConfiguration,
  factory: ActiveSessionFactory[AsyncContext, Player])
  extends TwoPhasedPlayerDataRepository[AsyncContext, SyncContext, ActiveSessionReference[AsyncContext, SyncContext]] {

  override protected type TemporaryData = Option[RemainingFlyDuration]

  // TODO DBに永続化した値を読み込む
  override protected val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], Option[RemainingFlyDuration]]] = {
    (_, _) => SyncEffect[SyncContext].pure(Right(None))
  }

  override protected def initializeValue(player: Player,
                                         temporaryData: Option[RemainingFlyDuration]
                                        ): SyncContext[ActiveSessionReference[AsyncContext, SyncContext]] = {
    ActiveSessionReference
      .createNew[AsyncContext, SyncContext]
      .flatTap { reference =>
        temporaryData match {
          case Some(duration) =>
            reference
              .replaceSession(factory.start[SyncContext](duration).run(player))
              .runAsync(_ => IO.unit)
              .runSync[SyncContext]
          case None => SyncEffect[SyncContext].unit
        }
      }
  }

  // TODO DBに永続化する値を書き込む
  override protected val unloadData: (Player, ActiveSessionReference[AsyncContext, SyncContext]) => SyncContext[Unit] = {
    (_, sessionRef) =>
      sessionRef.stopAnyRunningSession
        .runAsync(_ => IO.unit)
        .runSync[SyncContext]
  }
}
