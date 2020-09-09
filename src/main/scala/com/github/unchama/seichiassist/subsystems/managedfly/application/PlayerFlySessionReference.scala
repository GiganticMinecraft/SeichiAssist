package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{NotFlying, PlayerFlyStatus}

/**
 * プレーヤーの飛行セッションの参照
 */
class PlayerFlySessionReference[
  AsyncContext[_] : ConcurrentEffect,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
](private val sessionRef: Ref[SyncContext, Option[PlayerFlySession[AsyncContext, SyncContext]]]) {

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

  def replaceSessionWith(newSession: PlayerFlySession[AsyncContext, SyncContext]): AsyncContext[Unit] = {
    for {
      oldSessionOption <- sessionRef.getAndSet(Some(newSession)).coerceTo[AsyncContext]
      _ <- finishSessionIfPresent(oldSessionOption)
    } yield ()
  }

  def getLatestFlyStatus: SyncContext[PlayerFlyStatus] =
    for {
      sessionOption <- sessionRef.get
      status <- sessionOption match {
        case Some(session) => session.latestFlyStatus
        case None => Sync[SyncContext].pure(NotFlying)
      }
    } yield status
}

object PlayerFlySessionReference {

  import cats.implicits._

  def createNew[
    AsyncContext[_] : ConcurrentEffect,
    SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
  ]: SyncContext[PlayerFlySessionReference[AsyncContext, SyncContext]] = {
    for {
      ref <- Ref[SyncContext].of[Option[PlayerFlySession[AsyncContext, SyncContext]]](None)
    } yield new PlayerFlySessionReference(ref)
  }

}
