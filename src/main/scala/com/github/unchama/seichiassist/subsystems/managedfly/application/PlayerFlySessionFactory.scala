package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.effect.{Concurrent, ExitCase, Sync, Timer}
import com.github.unchama.generic.effect.concurrent.AsymmetricTryableFiber
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, PlayerFlyStatus, RemainingFlyDuration}

/**
 * プレーヤーに紐づいた、Flyセッションを作成できるオブジェクト。
 */
abstract class PlayerFlySessionFactory[AsyncContext[_] : Timer : Concurrent] {

  import cats.effect.implicits._
  import cats.implicits._

  import scala.concurrent.duration._

  protected sealed abstract class InternalException extends Throwable

  protected case object PlayerExpNotEnough extends InternalException

  protected case object FlyDurationExpired extends InternalException

  val ensurePlayerExp: AsyncContext[Unit]
  val consumePlayerExp: AsyncContext[Unit]

  val isPlayerIdle: AsyncContext[Boolean]

  val synchronizeFlyStatus: PlayerFlyStatus => AsyncContext[Unit]
  val handleInterruptions: InternalException => AsyncContext[Unit]

  private def tickDuration[F[_]](duration: RemainingFlyDuration)(implicit F: Sync[F]): F[RemainingFlyDuration] =
    duration.tickOneMinute match {
      case Some(tickedDuration) => F.pure(tickedDuration)
      case None => F.raiseError(FlyDurationExpired)
    }

  private def doOneMinuteCycle(duration: RemainingFlyDuration): AsyncContext[RemainingFlyDuration] = {
    Timer[AsyncContext].sleep(1.minute) >>
      isPlayerIdle.ifM(Sync[AsyncContext].unit, consumePlayerExp) >>
      tickDuration(duration)
  }

  def start(totalDuration: RemainingFlyDuration): AsyncContext[AsymmetricTryableFiber[AsyncContext, Nothing]] = {
    AsymmetricTryableFiber.start[AsyncContext, Nothing] {
      {
        ensurePlayerExp >>
          synchronizeFlyStatus(Flying(totalDuration)) >>
          totalDuration.iterateForeverM(doOneMinuteCycle)
      }.guaranteeCase {
        case ExitCase.Error(e: InternalException) => handleInterruptions(e)
        case _ => Monad[AsyncContext].unit
      }.guarantee {
        synchronizeFlyStatus(NotFlying)
      }
    }
  }
}
