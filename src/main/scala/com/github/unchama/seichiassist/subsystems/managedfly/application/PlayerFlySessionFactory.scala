package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ExitCase, Sync, Timer}
import com.github.unchama.concurrent.ReadOnlyRef
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.AsymmetricTryableFiber
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, RemainingFlyDuration}

/**
 * プレーヤーに紐づいたFlyセッションを作成できるオブジェクト。
 */
class PlayerFlySessionFactory[
  AsyncContext[_] : Timer : Concurrent,
  Player
](implicit KleisliAsyncContext: PlayerFlyStatusManipulation[Kleisli[AsyncContext, Player, *]]) {

  type KleisliAsyncContext[A] = Kleisli[AsyncContext, Player, A]

  import KleisliAsyncContext._
  import cats.effect.implicits._
  import cats.implicits._
  import com.github.unchama.generic.ContextCoercion._

  import scala.concurrent.duration._

  private def tickDuration[F[_]](duration: RemainingFlyDuration)(implicit F: Sync[F]): F[RemainingFlyDuration] =
    duration.tickOneMinute match {
      case Some(tickedDuration) => F.pure(tickedDuration)
      case None => F.raiseError(FlyDurationExpired)
    }

  private def doOneMinuteCycle(duration: RemainingFlyDuration): KleisliAsyncContext[RemainingFlyDuration] = {
    Timer[KleisliAsyncContext].sleep(1.minute) >>
      isPlayerIdle.ifM(Sync[KleisliAsyncContext].unit, consumePlayerExp) >>
      tickDuration[KleisliAsyncContext](duration)
  }

  def start[
    SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
  ](totalDuration: RemainingFlyDuration, player: Player): AsyncContext[PlayerFlySession[AsyncContext, SyncContext]] = {
    for {
      currentRemainingDurationRef <-
        Ref
          .of[SyncContext, RemainingFlyDuration](totalDuration)
          .coerceTo[AsyncContext]
      fiber <- AsymmetricTryableFiber.start[AsyncContext, Nothing] {
        {
          ensurePlayerExp.run(player) >>
            synchronizeFlyStatus(Flying(totalDuration)).run(player) >>
            totalDuration.iterateForeverM { duration =>
              doOneMinuteCycle(duration).run(player).flatTap { updatedDuration =>
                currentRemainingDurationRef.set(updatedDuration).coerceTo[AsyncContext]
              }
            }
        }.guaranteeCase {
          case ExitCase.Error(e: InternalInterruption) => sendNotificationsOnInterruption(e).run(player)
          case _ => Monad[AsyncContext].unit
        }.guarantee {
          synchronizeFlyStatus(NotFlying).run(player)
        }
      }
    } yield new PlayerFlySession(fiber, ReadOnlyRef.fromRef(currentRemainingDurationRef))
  }
}
