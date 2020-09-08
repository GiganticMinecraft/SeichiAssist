package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ConcurrentEffect, IO, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{NotFlying, PlayerFlyStatus, RemainingFlyDuration}

class PlayerFlySessionGateway[
  AsyncContext[_] : ConcurrentEffect,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
](private val sessionRef: Ref[SyncContext, Option[PlayerFlySession[AsyncContext, SyncContext]]],
  private val factory: PlayerFlySessionFactory[AsyncContext]) {

  import ContextCoercion._
  import cats.effect.implicits._
  import cats.implicits._

  private def finishSessionIfPresent(sessionOption: Option[PlayerFlySession[AsyncContext, SyncContext]]): AsyncContext[Unit] = {
    sessionOption match {
      case Some(session) => session.finish
      case None => Concurrent[AsyncContext].unit
    }
  }

  def stopAnyRunningSession: AsyncContext[Unit] =
    sessionRef.getAndSet(None).coerceTo[AsyncContext] >>= finishSessionIfPresent

  def getLatestFlyStatus: SyncContext[PlayerFlyStatus] =
    for {
      sessionOption <- sessionRef.get
      status <- sessionOption match {
        case Some(session) => session.latestFlyStatus
        case None => Sync[SyncContext].pure(NotFlying)
      }
    } yield status

  def startNewSessionOfDuration(duration: RemainingFlyDuration): AsyncContext[Unit] =
    for {
      newSession <- factory.start[SyncContext](duration)
      oldSessionOption <- sessionRef.getAndSet(Some(newSession)).coerceTo[AsyncContext]
      _ <- finishSessionIfPresent(oldSessionOption)
    } yield ()

  def startNewSessionAndForget(duration: RemainingFlyDuration): SyncContext[Unit] =
    startNewSessionOfDuration(duration).runAsync(_ => IO.unit).runSync[SyncContext]
}

object PlayerFlySessionGateway {

  import cats.implicits._

  def createNew[
    AsyncContext[_] : ConcurrentEffect,
    SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
  ](factory: PlayerFlySessionFactory[AsyncContext]): SyncContext[PlayerFlySessionGateway[AsyncContext, SyncContext]] = {
    for {
      ref <- Ref[SyncContext].of[Option[PlayerFlySession[AsyncContext, SyncContext]]](None)
    } yield new PlayerFlySessionGateway(ref, factory)
  }

}
