package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.Sync
import com.github.unchama.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.concurrent.AsymmetricTryableFiber
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, PlayerFlyStatus, RemainingFlyDuration}

class ActiveSession[
  AsyncContext[_] : Sync,
  SyncContext[_] : Sync
](sessionFiber: AsymmetricTryableFiber[AsyncContext, Nothing],
  latestRemainingDurationRef: ReadOnlyRef[SyncContext, RemainingFlyDuration]) {

  import cats.implicits._

  def finish: AsyncContext[Boolean] = sessionFiber.cancelIfRunning

  def waitForCompletion: AsyncContext[Unit] = sessionFiber.waitForResult.as(())

  def isActive: SyncContext[Boolean] = sessionFiber.isRunning[SyncContext]

  def latestFlyStatus: SyncContext[PlayerFlyStatus] = {
    for {
      sessionIsActive <- isActive
      latestFlyStatus <-
        if (sessionIsActive)
          latestRemainingDurationRef.read.map { duration =>
            Flying(duration)
          }
        else
          Sync[SyncContext].pure(NotFlying)
    } yield latestFlyStatus
  }
}
