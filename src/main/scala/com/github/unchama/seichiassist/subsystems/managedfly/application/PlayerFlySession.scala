package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.Sync
import com.github.unchama.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.concurrent.AsymmetricTryableFiber
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, PlayerFlyStatus, RemainingFlyDuration}

class PlayerFlySession[
  AsyncContext[_],
  SyncContext[_] : Sync
](sessionFiber: AsymmetricTryableFiber[AsyncContext, Nothing],
  latestRemainingDurationRef: ReadOnlyRef[SyncContext, RemainingFlyDuration]) {

  import cats.implicits._

  def finish: AsyncContext[Unit] = sessionFiber.cancel

  def isActive: SyncContext[Boolean] = sessionFiber.isIncomplete[SyncContext]

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
