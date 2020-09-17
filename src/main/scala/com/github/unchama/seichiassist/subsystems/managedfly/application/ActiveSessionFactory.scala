package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ExitCase, Sync, Timer}
import com.github.unchama.concurrent.ReadOnlyRef
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.AsymmetricTryableFiber
import com.github.unchama.seichiassist.subsystems.managedfly.domain._

/**
 * プレーヤーに紐づいたFlyセッションを作成できるオブジェクト。
 */
class ActiveSessionFactory[
  AsyncContext[_] : Timer : Concurrent,
  Player
](implicit KleisliAsyncContext: PlayerFlyStatusManipulation[Kleisli[AsyncContext, Player, *]]) {

  type KleisliAsyncContext[A] = Kleisli[AsyncContext, Player, A]

  private def tickDuration[F[_]](duration: RemainingFlyDuration)(implicit F: Sync[F]): F[RemainingFlyDuration] =
    duration.tickOneMinute match {
      case Some(tickedDuration) => F.pure(tickedDuration)
      case None => F.raiseError(FlyDurationExpired)
    }

  import KleisliAsyncContext._

  private def doOneMinuteCycle(duration: RemainingFlyDuration): KleisliAsyncContext[RemainingFlyDuration] = {
    import cats.implicits.catsSyntaxFlatMapOps

    import scala.concurrent.duration._

    for {
      oldIdleStatus <- isPlayerIdle
      _ <- notifyRemainingDuration(oldIdleStatus, duration)
      _ <- Timer[KleisliAsyncContext].sleep(1.minute)
      newIdleStatus <- isPlayerIdle
      newDuration <- newIdleStatus match {
        case Idle =>
          Kleisli.pure(duration)
        case HasMovedRecently => consumePlayerExp >> tickDuration[KleisliAsyncContext](duration)
      }
    } yield newDuration
  }

  def start[
    SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
  ](totalDuration: RemainingFlyDuration): KleisliAsyncContext[ActiveSession[AsyncContext, SyncContext]] = {
    import cats.effect.implicits._
    import cats.implicits._
    import com.github.unchama.generic.ContextCoercion._

    for {
      currentRemainingDurationRef <-
        Ref.in[KleisliAsyncContext, SyncContext, RemainingFlyDuration](totalDuration)

      kleisliAsyncUpdateRef = { duration: RemainingFlyDuration =>
        Kleisli.liftF {
          currentRemainingDurationRef.set(duration).coerceTo[AsyncContext]
        }
      }

      fiber <- Kleisli { player: Player =>
        AsymmetricTryableFiber.start[AsyncContext, Nothing] {
          {
            ensurePlayerExp >>
              synchronizeFlyStatus(Flying(totalDuration)) >>
              totalDuration.iterateForeverM { duration =>
                doOneMinuteCycle(duration).flatTap(kleisliAsyncUpdateRef)
              }
          }.guaranteeCase {
            case ExitCase.Error(e: InternalInterruption) => sendNotificationsOnInterruption(e)
            case _ => Monad[KleisliAsyncContext].unit
          }.guarantee {
            synchronizeFlyStatus(NotFlying)
          }.run(player)
        }
      }
    } yield new ActiveSession(fiber, ReadOnlyRef.fromRef(currentRemainingDurationRef))
  }
}
