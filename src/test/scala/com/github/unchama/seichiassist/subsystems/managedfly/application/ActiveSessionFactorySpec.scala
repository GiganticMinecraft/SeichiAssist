package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.Monad
import cats.effect.{SyncIO, Timer}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, HasMovedRecently, Idle, RemainingFlyDuration}
import com.github.unchama.testutil.concurrent.tests.{ConcurrentEffectTest, TaskDiscreteEventually}
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import monix.catnap.SchedulerEffect
import monix.eval.Task
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ActiveSessionFactorySpec
  extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers
    with TaskDiscreteEventually
    with ConcurrentEffectTest
    with MonixTestSchedulerTests {

  import com.github.unchama.generic.ContextCoercion._

  import scala.concurrent.duration._

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 10.millis)
  implicit override val discreteEventuallyConfig: DiscreteEventuallyConfig = DiscreteEventuallyConfig(10000)

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
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        // when
        _ <- session.finish
        // then
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 30.seconds)
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
        _ <- discreteEventually {
          Task {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe true
          }
        }

        // cleanup
        _ <- session.finish
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.second)
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
        _ <- discreteEventually {
          Task {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.second)
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
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.second)
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
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }
        _ <- session.finish

        // then
        _ <- discreteEventually {
          Task {
            playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe originalExp
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.minute)
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
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        // then
        _ <- monixTimer.sleep(30.seconds)
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - sleptMinute * 100)

          for {
            _ <- discreteEventually {
              Task {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)

        // cleanup
        _ <- session.finish
      } yield ()

      awaitForProgram(runConcurrent(program)(100), minutesToWait.minutes + 30.seconds)
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
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        // then
        _ <- monixTimer.sleep(30.seconds)
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - sleptMinute * 100)

          for {
            _ <- discreteEventually {
              Task {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(true))
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - minutesToWait * 100)

          for {
            _ <- discreteEventually {
              Task {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(false))
        _ <- Monad[Task].iterateWhileM(0) { sleptMinute =>
          val expectedExperience = FiniteNonNegativeExperience(originalExp - (minutesToWait + sleptMinute) * 100)

          for {
            _ <- discreteEventually {
              Task {
                playerRef.experienceMutex.readLatest.unsafeRunSync() shouldBe expectedExperience
              }
            }
            _ <- monixTimer.sleep(1.minute)
          } yield sleptMinute + 1
        }(_ < minutesToWait)

        // cleanup
        _ <- session.finish
      } yield ()

      awaitForProgram(runConcurrent(program)(100), (minutesToWait * 3).minutes + 30.seconds)
    }


    "not tick whenever player is idle" in {
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
        session <- factory.start[SyncIO](RemainingFlyDuration.PositiveMinutes.fromPositive(100)).run(playerRef)

        // セッションが有効になるまで待つ
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        // when
        _ <- monixTimer.sleep(50.minutes)

        // then
        _ <- discreteEventually {
          Task {
            session.latestFlyStatus.unsafeRunSync() shouldBe Flying(RemainingFlyDuration.PositiveMinutes.fromPositive(50))
          }
        }

        // when
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(true))
        _ <- monixTimer.sleep(50.minutes)

        // then
        _ <- discreteEventually {
          Task {
            session.latestFlyStatus.unsafeRunSync() shouldBe Flying(RemainingFlyDuration.PositiveMinutes.fromPositive(50))
          }
        }

        // when
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(false))
        _ <- monixTimer.sleep(50.minutes)

        // then
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 150.minutes)
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
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe true
          }
        }

        _ <- session.waitForCompletion

        // then
        endTime <- monixTimer.clock.realTime(SECONDS)

        _ <- Task {
          (endTime - initialTime) shouldBe 11.minutes.toSeconds
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 11.minutes)
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
        _ <- discreteEventually {
          Task {
            playerRef.messageLog.readLatest.unsafeRunSync().last shouldBe InterruptionMessageMock(PlayerExpNotEnough)
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.second)
    }

    "send appropriate notification of remaining fly time every minute" in {
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
        _ <- factory.start[SyncIO](RemainingFlyDuration.PositiveMinutes.fromPositive(10)).run(playerRef)
        _ <- monixTimer.sleep(4.minutes + 30.seconds)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(true))
        _ <- monixTimer.sleep(2.minutes)
        _ <- playerRef.isIdleMutex.lockAndUpdate(_ => Task.pure(false))
        _ <- monixTimer.sleep(5.minutes + 30.seconds)

        // then
        _ <- discreteEventually {
          Task {
            playerRef.messageLog.readLatest.unsafeRunSync() shouldBe Vector(
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(10)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(9)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(8)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(7)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(6)),
              StatusMessageMock(Idle, RemainingFlyDuration.PositiveMinutes.fromPositive(6)),
              StatusMessageMock(Idle, RemainingFlyDuration.PositiveMinutes.fromPositive(6)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(5)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(4)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(3)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(2)),
              StatusMessageMock(HasMovedRecently, RemainingFlyDuration.PositiveMinutes.fromPositive(1)),
              InterruptionMessageMock(FlyDurationExpired)
            )
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 12.minutes)
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
        _ <- discreteEventually {
          Task {
            session.isActive.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), sessionLengthInMinutes.minutes + 30.seconds)
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
        _ <- discreteEventually {
          Task {
            playerRef.messageLog.readLatest.unsafeRunSync().last shouldBe InterruptionMessageMock(FlyDurationExpired)
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), sessionLengthInMinutes.minutes + 30.seconds)
    }
  }
}
