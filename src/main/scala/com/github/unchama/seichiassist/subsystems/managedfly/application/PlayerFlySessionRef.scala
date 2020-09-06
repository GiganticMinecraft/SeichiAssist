package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{PlayerFlyStatus, RemainingFlyDuration}

class PlayerFlySessionRef[
  AsyncContext[_] : Concurrent,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
](private val sessionRef: Ref[SyncContext, PlayerFlySession[AsyncContext, SyncContext]],
  private val factory: PlayerFlySessionFactory[AsyncContext]) {

  import ContextCoercion._
  import cats.implicits._

  def stopAnyRunningSession: AsyncContext[Unit] = sessionRef.get.coerceTo[AsyncContext] >>= (_.finish)

  def getCurrentStatus: SyncContext[PlayerFlyStatus] = sessionRef.get >>= (_.latestFlyStatus)

  def startNewSessionOfDuration(duration: RemainingFlyDuration): AsyncContext[Unit] = {
    for {
      newSession <- factory.start[SyncContext](duration).coerceTo[AsyncContext]
      oldSession <- sessionRef.getAndSet(newSession).coerceTo[AsyncContext]
      _ <- oldSession.finish
    } yield ()
  }
}
