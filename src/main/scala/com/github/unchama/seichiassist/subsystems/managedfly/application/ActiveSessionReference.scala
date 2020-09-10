package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.{Concurrent, ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.Mutex
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{NotFlying, PlayerFlyStatus}

/**
 * プレーヤーの飛行セッションの参照
 */
class ActiveSessionReference[
  AsyncContext[_] : ConcurrentEffect,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
](private val sessionMutexRef: Mutex[AsyncContext, SyncContext, Option[ActiveSession[AsyncContext, SyncContext]]]) {

  import cats.implicits._

  private def finishSessionIfPresent(sessionOption: Option[ActiveSession[AsyncContext, SyncContext]]): AsyncContext[Unit] = {
    sessionOption match {
      case Some(session) => session.finish
      case None => Concurrent[AsyncContext].unit
    }
  }

  def stopAnyRunningSession: AsyncContext[Unit] =
    sessionMutexRef.lockAndModify(finishSessionIfPresent(_).as(None, ()))

  def replaceSession(createSession: AsyncContext[ActiveSession[AsyncContext, SyncContext]]): AsyncContext[Unit] =
    sessionMutexRef.lockAndModify { sessionOption =>
      for {
        session <- finishSessionIfPresent(sessionOption) >> createSession
      } yield (Some(session), ())
    }

  def getLatestFlyStatus: SyncContext[PlayerFlyStatus] =
    for {
      sessionOption <- sessionMutexRef.readLatest
      status <- sessionOption match {
        case Some(session) => session.latestFlyStatus
        case None => Sync[SyncContext].pure(NotFlying)
      }
    } yield status
}

object ActiveSessionReference {

  import cats.implicits._

  def createNew[
    AsyncContext[_] : ConcurrentEffect,
    SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
  ]: SyncContext[ActiveSessionReference[AsyncContext, SyncContext]] = {
    for {
      mutex <- Mutex.of[AsyncContext, SyncContext, Option[ActiveSession[AsyncContext, SyncContext]]](None)
    } yield new ActiveSessionReference(mutex)
  }

}
