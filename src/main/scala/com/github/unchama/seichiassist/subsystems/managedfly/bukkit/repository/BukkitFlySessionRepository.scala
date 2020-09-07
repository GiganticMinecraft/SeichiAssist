package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.repository

import java.util.UUID

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, IO, SyncEffect, Timer}
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.managedfly.application.{PlayerFlySession, PlayerFlySessionRef, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.managedfly.bukkit.application.UuidBasedPlayerFlySessionFactory
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import org.bukkit.entity.Player

class BukkitFlySessionRepository[
  AsyncContext[_] : ConcurrentEffect : MinecraftServerThreadShift : Timer,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
](implicit effectEnvironment: EffectEnvironment, configuration: SystemConfiguration)
  extends TwoPhasedPlayerDataRepository[AsyncContext, SyncContext, PlayerFlySessionRef[AsyncContext, SyncContext]] {

  override protected type TemporaryData = Option[RemainingFlyDuration]

  // TODO DBに永続化した値を読み込む
  override protected val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], Option[RemainingFlyDuration]]] = {
    (_, _) => SyncEffect[SyncContext].pure(Right(None))
  }

  import cats.effect.implicits._
  import cats.implicits._

  override protected def initializeValue(player: Player,
                                         temporaryData: Option[RemainingFlyDuration]
                                        ): SyncContext[PlayerFlySessionRef[AsyncContext, SyncContext]] = {
    val factory: UuidBasedPlayerFlySessionFactory[AsyncContext] = {
      new UuidBasedPlayerFlySessionFactory[AsyncContext](player.getUniqueId, configuration.expConsumptionAmount)
    }

    val createSessionRef: SyncContext[PlayerFlySessionRef[AsyncContext, SyncContext]] =
      for {
        ref <- Ref[SyncContext].of[Option[PlayerFlySession[AsyncContext, SyncContext]]](None)
      } yield new PlayerFlySessionRef(ref, factory)

    for {
      sessionRef <- createSessionRef
      _ <- temporaryData match {
        case Some(duration) =>
          sessionRef
            .startNewSessionOfDuration(duration)
            .runAsync(_ => IO.unit)
            .runSync[SyncContext]
        case None =>
          SyncEffect[SyncContext].unit
      }
    } yield sessionRef
  }

  // TODO DBに永続化する値を書き込む
  override protected val unloadData: (Player, PlayerFlySessionRef[AsyncContext, SyncContext]) => SyncContext[Unit] = {
    (_, sessionRef) =>
      sessionRef.stopAnyRunningSession
        .runAsync(_ => IO.unit)
        .runSync[SyncContext]
  }
}
