package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.effect.{SyncIO, Timer}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import com.github.unchama.testutil.concurrent.tests.{EventuallyF, ParallelEffectTest}
import monix.catnap.SchedulerEffect
import monix.eval.Task
import monix.execution.schedulers.TestScheduler
import org.scalatest.Assertion
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.Await

class ActiveSessionFactorySpec
  extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers with ScalaFutures with EventuallyF
    with ParallelEffectTest {

  import com.github.unchama.generic.ContextCoercion._

  import scala.concurrent.duration._

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 10.millis)

  implicit val monixScheduler: TestScheduler = TestScheduler()
  implicit val monixTimer: Timer[Task] = SchedulerEffect.timer(monixScheduler)

  val mock = new Mock[Task, SyncIO]

  import mock._

  "Fly session" should {
    "be able to tell if it is active or not" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // then
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe true
        }

        // when
        _ <- session.finish
        // then
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe false
        }
      } yield ()

      program.runToFuture.futureValue
    }

    "synchronize player's fly status once started" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)

        // then
        _ <- eventuallyF[Task, Assertion] {
          playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe true
        }

        // cleanup
        _ <- session.finish
      } yield ()

      program.runToFuture.futureValue
    }

    "synchronize player's fly status when cancelled or complete" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        _ <- session.finish

        // then
        _ <- eventuallyF[Task, Assertion] {
          playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe false
        }
      } yield ()

      program.runToFuture.futureValue
    }

    "terminate immediately if the player does not have enough experience" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(99),
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)

        // then
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe false
        }
      } yield ()

      program.runToFuture.futureValue
    }

    "not consume player experience in first 1 minute even if terminated" in {
      val originalExp = FiniteNonNegativeExperience(150)

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          initialExperience = originalExp,
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe true
        }
        _ <- session.finish

        // then
        _ <- eventuallyF[Task, Assertion] {
          playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe originalExp
        }
      } yield ()

      program.runToFuture.futureValue
    }

    "consume player experience every minute as specified by the configuration" in {
      val originalExp = 100000
      val minutesToWait = 100

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(originalExp),
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe true
        }

        // then
        _ <- monixTimer.sleep(30.seconds)
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - sleptMinute * 100)

          for {
            _ <- eventuallyF[Task, Assertion] {
              playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)

        // cleanup
        _ <- session.finish
      } yield ()

      val programs = program

      val future = programs.runToFuture

      monixScheduler.tick(30.seconds)
      for (_ <- 0 to minutesToWait) {
        monixScheduler.tick(1.minute)
      }

      Await.result(future, 30.seconds)
    }

    "not consume player experience whenever player is idle" in {
      val originalExp = 100000
      val minutesToWait = 100

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(originalExp),
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe true
        }

        // then
        _ <- monixTimer.sleep(30.seconds)
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - sleptMinute * 100)

          for {
            _ <- eventuallyF[Task, Assertion] {
              playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(true))
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - minutesToWait * 100)

          for {
            _ <- eventuallyF[Task, Assertion] {
              playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(false))
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - (minutesToWait + sleptMinute) * 100)

          for {
            _ <- eventuallyF[Task, Assertion] {
              playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)

        // cleanup
        _ <- session.finish
      } yield ()

      val programs = program

      val future = programs.runToFuture

      monixScheduler.tick(30.seconds)
      for (_ <- 0 to minutesToWait * 3) {
        monixScheduler.tick(1.minute)
      }

      Await.result(future, 30.seconds)
    }

    "terminate when player's experience is below per-minute experience consumption" in {
      val originalExp = 10000

      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 1000
        )

      // 消費は丁度10回でき、11回目の経験値チェックで飛行セッションが閉じるべきなので、
      // 11分の経過を期待する。

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(originalExp),
          initiallyIdle = false
        ).coerceTo[Task]

        initialTime <- monixTimer.clock.realTime(SECONDS)

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        // セッションが有効になるまで待つ
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe true
        }

        _ <- session.waitForCompletion

        // then
        endTime <- monixTimer.clock.realTime(SECONDS)

        _ <- Task {
          (endTime - initialTime) shouldBe 11.minutes.toSeconds
        }
      } yield ()

      val programs = program

      val future = programs.runToFuture

      for (_ <- 1 to 11) {
        monixScheduler.tick(1.minute)
      }

      Await.result(future, 30.seconds)
    }

    "send appropriate notification when player does not have enough experience" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          FiniteNonNegativeExperience(99),
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        _ <- session.waitForCompletion

        // then
        _ <- eventuallyF[Task, Assertion] {
          playerRef.messageLog.readLatest.unsafeRunSync() shouldBe Vector(MessageMock(PlayerExpNotEnough))
        }
      } yield ()

      program.runToFuture.futureValue
    }
  }

  "Finite fly session" should {
    "terminate exactly when the minute specified has passed if the player has enough experience" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      val sessionLengthInMinutes = 10
      val originalSessionLength = RemainingFlyDuration.PositiveMinutes.fromPositive(sessionLengthInMinutes)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        session <- factory.start[SyncIO](originalSessionLength).run(playerRef)

        _ <- monixTimer.sleep(sessionLengthInMinutes.minutes + 30.seconds)

        // then
        _ <- eventuallyF[Task, Assertion] {
          session.isActive.unsafeRunSync() shouldBe false
        }
      } yield ()

      val programs = program

      val future = programs.runToFuture

      monixScheduler.tick(30.seconds)
      for (_ <- 1 to 11) {
        monixScheduler.tick(1.minute)
      }

      Await.result(future, 30.seconds)
    }

    "send appropriate notification when a session expires" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 100
        )

      val sessionLengthInMinutes = 10
      val originalSessionLength = RemainingFlyDuration.PositiveMinutes.fromPositive(sessionLengthInMinutes)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]

        // when
        _ <- factory.start[SyncIO](originalSessionLength).run(playerRef)

        _ <- monixTimer.sleep(sessionLengthInMinutes.minutes + 30.seconds)

        // then
        _ <- eventuallyF[Task, Assertion] {
          playerRef.messageLog.readLatest.unsafeRunSync() shouldBe Vector(MessageMock(FlyDurationExpired))
        }
      } yield ()

      val programs = program

      val future = programs.runToFuture

      monixScheduler.tick(30.seconds)
      for (_ <- 1 to 11) {
        monixScheduler.tick(1.minute)
      }

      Await.result(future, 30.seconds)
    }
  }
}
