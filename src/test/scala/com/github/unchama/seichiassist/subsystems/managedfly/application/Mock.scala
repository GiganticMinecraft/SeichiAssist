package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.data.Kleisli
import cats.effect.{Concurrent, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.Mutex
import com.github.unchama.seichiassist.subsystems.managedfly.domain._

private[managedfly] class Mock[
  AsyncContext[_] : Concurrent,
  SyncContext[_] : Sync : ContextCoercion[*[_], AsyncContext]
] {

  sealed trait ExperienceMock {
    def consume(amount: BigInt): Option[ExperienceMock]
  }

  case class FiniteNonNegativeExperience(internalAmount: BigInt) extends ExperienceMock {
    require(internalAmount >= 0)

    override def consume(amount: BigInt): Option[ExperienceMock] =
      if (amount <= internalAmount) Some(FiniteNonNegativeExperience(internalAmount - amount)) else None
  }

  case object InfiniteExperience extends ExperienceMock {
    override def consume(amount: BigInt): Option[ExperienceMock] = Some(InfiniteExperience)
  }

  case class MessageMock(interruption: InternalInterruption)

  import ContextCoercion._
  import cats.implicits._

  case class PlayerMockReference(isFlyingMutex: Mutex[AsyncContext, SyncContext, Boolean],
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

  object PlayerMockReference {
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

  def playerMockFlyStatusManipulation(implicit configuration: SystemConfiguration): PlayerFlyStatusManipulation[PlayerAsyncKleisli] = {
    new PlayerFlyStatusManipulation[PlayerAsyncKleisli] {
      override val ensurePlayerExp: PlayerAsyncKleisli[Unit] = Kleisli { player: PlayerMockReference =>
        player
          .experienceMutex
          .lockAndUpdate { experience =>
            experience.consume(configuration.expConsumptionAmount) match {
              case Some(_) => Monad[AsyncContext].pure(experience)
              case None => Sync[AsyncContext].raiseError(PlayerExpNotEnough)
            }
          }
          .as(())
      }

      override val consumePlayerExp: PlayerAsyncKleisli[Unit] = Kleisli { player: PlayerMockReference =>
        player
          .experienceMutex
          .lockAndUpdate { experience =>
            experience.consume(configuration.expConsumptionAmount) match {
              case Some(consumed) => Monad[AsyncContext].pure(consumed)
              case None => Sync[AsyncContext].raiseError(PlayerExpNotEnough)
            }
          }
          .as(())
      }

      override val isPlayerIdle: PlayerAsyncKleisli[IdleStatus] = Kleisli { player: PlayerMockReference =>
        player.isIdleMutex.readLatest.coerceTo[AsyncContext].map {
          if (_) Idle else HasMovedRecently
        }
      }

      override val synchronizeFlyStatus: PlayerFlyStatus => PlayerAsyncKleisli[Unit] = {
        case Flying(_) =>
          Kleisli { player: PlayerMockReference =>
            player.isFlyingMutex.lockAndUpdate(_ => Monad[AsyncContext].pure(true)).as(())
          }
        case NotFlying =>
          Kleisli { player: PlayerMockReference =>
            player.isFlyingMutex.lockAndUpdate(_ => Monad[AsyncContext].pure(false)).as(())
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
