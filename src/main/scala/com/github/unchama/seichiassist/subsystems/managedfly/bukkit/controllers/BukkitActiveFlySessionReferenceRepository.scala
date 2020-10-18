package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.controllers

import java.util.UUID

import cats.effect.{ConcurrentEffect, IO, SyncEffect, Timer}
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.managedfly.application.{ActiveSessionFactory, ActiveSessionReference, FlyDurationPersistenceRepository, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{PlayerFlyStatus, RemainingFlyDuration}
import org.bukkit.entity.Player

class BukkitActiveFlySessionReferenceRepository[
  AsyncContext[_] : ConcurrentEffect : MinecraftServerThreadShift : Timer,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
](implicit effectEnvironment: EffectEnvironment,
  configuration: SystemConfiguration,
  factory: ActiveSessionFactory[AsyncContext, Player],
  persistenceRepository: FlyDurationPersistenceRepository[SyncContext, UUID])
  extends TwoPhasedPlayerDataRepository[AsyncContext, SyncContext, ActiveSessionReference[AsyncContext, SyncContext]] {

  override protected type TemporaryData = Option[RemainingFlyDuration]

  import cats.implicits._

  override protected val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], Option[RemainingFlyDuration]]] = {
    (_, uuid) =>
      for {
        result <- persistenceRepository.read(uuid).attempt
      } yield result.leftMap { _ =>
        Some("飛行データの読み込みに失敗しました。")
      }
  }

  import cats.effect.implicits._
  import cats.implicits._

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

  override protected val unloadData: (Player, ActiveSessionReference[AsyncContext, SyncContext]) => SyncContext[Unit] = {
    (player, sessionRef) =>
      for {
        latestStatus <- sessionRef.getLatestFlyStatus
        _ <- persistenceRepository.writePair(player.getUniqueId, PlayerFlyStatus.toDurationOption(latestStatus))
        _ <- sessionRef.stopAnyRunningSession
          .runAsync(_ => IO.unit)
          .runSync[SyncContext]
      } yield ()
  }
}
