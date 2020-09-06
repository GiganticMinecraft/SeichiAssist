package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{NotFlying, PlayerFlyStatus, RemainingFlyDuration}

class PlayerFlySessionRef[
  AsyncContext[_] : Concurrent,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
](private val sessionRef: Ref[SyncContext, Option[PlayerFlySession[AsyncContext, SyncContext]]],
  private val factory: PlayerFlySessionFactory[AsyncContext]) {

  import ContextCoercion._
  import cats.implicits._

  private def finishSessionIfPresent(sessionOption: Option[PlayerFlySession[AsyncContext, SyncContext]]): AsyncContext[Unit] = {
    sessionOption match {
      case Some(session) => session.finish
      case None => Concurrent[AsyncContext].unit
    }
  }

  def stopAnyRunningSession: AsyncContext[Unit] =
    sessionRef.getAndSet(None).coerceTo[AsyncContext] >>= finishSessionIfPresent

  def getCurrentStatus: SyncContext[PlayerFlyStatus] =
    for {
      sessionOption <- sessionRef.get
      status <- sessionOption match {
        case Some(session) => session.latestFlyStatus
        case None => Sync[SyncContext].pure(NotFlying)
      }
    } yield status

  def startNewSessionOfDuration(duration: RemainingFlyDuration): AsyncContext[Unit] =
    for {
      newSession <- factory.start[SyncContext](duration).coerceTo[AsyncContext]
      oldSessionOption <- sessionRef.getAndSet(Some(newSession)).coerceTo[AsyncContext]
      _ <- finishSessionIfPresent(oldSessionOption)
    } yield ()
}
