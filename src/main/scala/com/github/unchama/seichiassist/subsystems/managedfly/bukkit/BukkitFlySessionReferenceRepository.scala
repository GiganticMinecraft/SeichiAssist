package com.github.unchama.seichiassist.subsystems.managedfly.bukkit

import java.util.UUID

import cats.effect.{ConcurrentEffect, IO, SyncEffect, Timer}
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.managedfly.application.{PlayerFlySessionFactory, PlayerFlySessionReference, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import org.bukkit.entity.Player

class BukkitFlySessionReferenceRepository[
  AsyncContext[_] : ConcurrentEffect : MinecraftServerThreadShift : Timer,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
](implicit effectEnvironment: EffectEnvironment,
  configuration: SystemConfiguration,
  factory: PlayerFlySessionFactory[AsyncContext, Player])
  extends TwoPhasedPlayerDataRepository[AsyncContext, SyncContext, PlayerFlySessionReference[AsyncContext, SyncContext]] {

  override protected type TemporaryData = Option[RemainingFlyDuration]

  // TODO DBに永続化した値を読み込む
  override protected val loadTemporaryData: (String, UUID) => SyncContext[Either[Option[String], Option[RemainingFlyDuration]]] = {
    (_, _) => SyncEffect[SyncContext].pure(Right(None))
  }

  import cats.effect.implicits._
  import cats.implicits._

  override protected def initializeValue(player: Player,
                                         temporaryData: Option[RemainingFlyDuration]
                                        ): SyncContext[PlayerFlySessionReference[AsyncContext, SyncContext]] = {
    PlayerFlySessionReference
      .createNew[AsyncContext, SyncContext]
      .flatTap { reference =>
        temporaryData match {
          case Some(duration) => {
            for {
              newSession <- factory.start[SyncContext](duration, player)
              _ <- reference.replaceSessionWith(newSession)
            } yield ()
          }.runAsync(_ => IO.unit).runSync[SyncContext]
          case None => SyncEffect[SyncContext].unit
        }
      }
  }

  // TODO DBに永続化する値を書き込む
  override protected val unloadData: (Player, PlayerFlySessionReference[AsyncContext, SyncContext]) => SyncContext[Unit] = {
    (_, sessionRef) =>
      sessionRef.stopAnyRunningSession
        .runAsync(_ => IO.unit)
        .runSync[SyncContext]
  }
}
