package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ConcurrentEffect, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{NotFlying, PlayerFlyStatus}

/**
 * プレーヤーの飛行セッションの参照
 */
class ActiveSessionReference[
  AsyncContext[_] : ConcurrentEffect,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
](private val sessionRef: Ref[SyncContext, Option[ActiveSession[AsyncContext, SyncContext]]]) {

  import ContextCoercion._
  import cats.implicits._

  private def finishSessionIfPresent(sessionOption: Option[ActiveSession[AsyncContext, SyncContext]]): AsyncContext[Unit] = {
    sessionOption match {
      case Some(session) => session.finish
      case None => Concurrent[AsyncContext].unit
    }
  }

  def stopAnyRunningSession: AsyncContext[Unit] =
    sessionRef.getAndSet(None).coerceTo[AsyncContext] >>= finishSessionIfPresent

  def replaceSessionWith(newSession: ActiveSession[AsyncContext, SyncContext]): AsyncContext[Unit] = {
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

object ActiveSessionReference {

  import cats.implicits._

  def createNew[
    AsyncContext[_] : ConcurrentEffect,
    SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
  ]: SyncContext[ActiveSessionReference[AsyncContext, SyncContext]] = {
    for {
      ref <- Ref[SyncContext].of[Option[ActiveSession[AsyncContext, SyncContext]]](None)
    } yield new ActiveSessionReference(ref)
  }

}
