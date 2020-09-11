package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.data.Kleisli
import cats.effect.{Async, Concurrent, ContextShift, IO, Sync, SyncIO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.Mutex
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, PlayerFlyStatus}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext

class ActiveSessionFactorySpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val shift: ContextShift[IO] = IO.contextShift(ec)
  val mock = new ActiveSessionFactoryMock[IO, SyncIO]

  "Fly session" should {
    "synchronize player's fly status once started" in {

    }
    "synchronize player's fly status when cancelled or complete" in {

    }
    "terminate immediately if the player does not have enough experience" in {

    }
    "not consume player experience in first 1 minute even if terminated" in {

    }
    "not consume player experience if player is idle" in {

    }
    "consume player experience every minute as specified by the configuration" in {

    }
    "terminate exactly when player's experience is below per-minute experience consumption" in {

    }
    "send appropriate notifications when interrupted" in {

    }
  }

  "Infinite fly session" should {
    "not terminate if the player has enough experience" in {

    }
  }

  "Finite fly session" should {
    "terminate exactly when the minute specified has passed if the player has enough experience" in {

    }
  }
}

private[managedfly] class ActiveSessionFactoryMock[
  AsyncContext[_] : Concurrent,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
] {

  private[managedfly] sealed trait ExperienceMock {
    def consume(amount: BigInt): Option[ExperienceMock]
  }

  private[managedfly] case class FiniteNonNegativeExperience(internalAmount: BigInt) extends ExperienceMock {
    require(internalAmount >= 0)

    override def consume(amount: BigInt): Option[ExperienceMock] =
      if (amount <= internalAmount) Some(FiniteNonNegativeExperience(internalAmount - amount)) else None
  }

  private[managedfly] case object InfiniteExperience extends ExperienceMock {
    override def consume(amount: BigInt): Option[ExperienceMock] = Some(InfiniteExperience)
  }

  private[managedfly] case class MessageMock(interruption: InternalInterruption)

  import ContextCoercion._
  import cats.implicits._

  private[managedfly] case class PlayerMockReference(isFlyingMutex: Mutex[AsyncContext, SyncContext, Boolean],
                                                     experienceMutex: Mutex[AsyncContext, SyncContext, ExperienceMock],
                                                     isIdleMutex: Mutex[AsyncContext, SyncContext, Boolean],
                                                     messageLog: Mutex[AsyncContext, SyncContext, Vector[MessageMock]]) {
    def sendMessage(message: MessageMock): AsyncContext[Unit] = {
      messageLog
        .lockAndUpdate { current =>
          Monad[AsyncContext].pure(current.appended(message))
        }
        .as(())
    }
  }

  private[managedfly] object PlayerMockReference {
    def apply(initiallyFlying: Boolean,
              initialExperience: ExperienceMock,
              initiallyIdle: Boolean): SyncContext[PlayerMockReference] =
      for {
        isFlyingMutex <- Mutex.of[AsyncContext, SyncContext, Boolean](initiallyFlying)
        experienceMutex <- Mutex.of[AsyncContext, SyncContext, ExperienceMock](initialExperience)
        idleMutex <- Mutex.of[AsyncContext, SyncContext, Boolean](initiallyIdle)
        messageLog <- Mutex.of[AsyncContext, SyncContext, Vector[MessageMock]](Vector())
      } yield {
        PlayerMockReference(isFlyingMutex, experienceMutex, idleMutex, messageLog)
      }
  }

  type PlayerAsyncKleisli[R] = Kleisli[AsyncContext, PlayerMockReference, R]

  implicit def playerMockFlyStatusManipulation(implicit configuration: SystemConfiguration)
  : PlayerFlyStatusManipulation[PlayerAsyncKleisli] = {

    new PlayerFlyStatusManipulation[PlayerAsyncKleisli] {
      override val ensurePlayerExp: PlayerAsyncKleisli[Unit] = Kleisli { player: PlayerMockReference =>
        player
          .experienceMutex
          .lockAndUpdate {
            case originalExperience@FiniteNonNegativeExperience(_) =>
              originalExperience.consume(configuration.expConsumptionAmount) match {
                case Some(_) =>
                  Monad[AsyncContext].pure(originalExperience)
                case None =>
                  Async[AsyncContext].raiseError(PlayerExpNotEnough)
              }
            case InfiniteExperience =>
              Monad[AsyncContext].pure(InfiniteExperience)
          }
          .as(())
      }

      override val consumePlayerExp: PlayerAsyncKleisli[Unit] = Kleisli { player: PlayerMockReference =>
        player
          .experienceMutex
          .lockAndUpdate {
            case originalExperience@FiniteNonNegativeExperience(_) =>
              originalExperience.consume(configuration.expConsumptionAmount) match {
                case Some(consumed) =>
                  Monad[AsyncContext].pure(consumed)
                case None =>
                  Async[AsyncContext].raiseError(PlayerExpNotEnough)
              }
            case InfiniteExperience =>
              Monad[AsyncContext].pure(InfiniteExperience)
          }
          .as(())
      }

      override val isPlayerIdle: PlayerAsyncKleisli[Boolean] = Kleisli { player: PlayerMockReference =>
        player.isIdleMutex.readLatest.coerceTo[AsyncContext]
      }

      override val synchronizeFlyStatus: PlayerFlyStatus => PlayerAsyncKleisli[Unit] = {
        case Flying(_) =>
          Kleisli { player: PlayerMockReference =>
            player.isFlyingMutex.lockAndUpdate(_ => Monad[AsyncContext].pure(false)).as(())
          }
        case NotFlying =>
          Kleisli { player: PlayerMockReference =>
            player.isFlyingMutex.lockAndUpdate(_ => Monad[AsyncContext].pure(true)).as(())
          }
      }

      override val sendNotificationsOnInterruption: InternalInterruption => PlayerAsyncKleisli[Unit] = { interruption =>
        Kleisli { player: PlayerMockReference =>
          player.sendMessage(MessageMock(interruption))
        }
      }
    }
  }
}
