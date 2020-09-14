package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.{SyncIO, Timer}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, RemainingFlyDuration}
import com.github.unchama.testutil.concurrent.tests.{ConcurrentEffectTest, TaskDiscreteEventually}
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import monix.catnap.SchedulerEffect
import monix.eval.Task
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ActiveSessionReferenceSpec
  extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers
    with TaskDiscreteEventually
    with ConcurrentEffectTest
    with MonixTestSchedulerTests {

  import com.github.unchama.generic.ContextCoercion._

  import scala.concurrent.duration._

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 10.millis)
  implicit override val discreteEventuallyConfig: DiscreteEventuallyConfig = DiscreteEventuallyConfig(1000000)

  implicit val monixScheduler: TestScheduler = TestScheduler()
  implicit val monixTimer: Timer[Task] = SchedulerEffect.timer(monixScheduler)

  val mock = new Mock[Task, SyncIO]

  import mock._

  "New fly session reference" should {
    "not have started any session" in {
      val program = for {
        sessionRef <- ActiveSessionReference.createNew[Task, SyncIO]
        status <- sessionRef.getLatestFlyStatus
      } yield status

      program.unsafeRunSync() shouldBe NotFlying
    }
  }

  "Active fly session reference" should {
    "correctly expose the fly status of a started session" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val sessionLengthInMinutes = 10
      val sessionDuration = RemainingFlyDuration.PositiveMinutes.fromPositive(sessionLengthInMinutes)

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]
        sessionRef <- ActiveSessionReference.createNew[Task, SyncIO].coerceTo[Task]

        createSession = factory.start[SyncIO](sessionDuration)

        // when
        _ <- sessionRef.replaceSession(createSession.run(playerRef))

        // then
        _ <- discreteEventually {
          Task {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(sessionDuration)
          }
        }

        // when
        _ <- monixTimer.sleep(sessionLengthInMinutes.minutes)

        // then
        _ <- discreteEventually {
          Task {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe NotFlying
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), sessionLengthInMinutes.minutes)
    }

    "be able to stop a running session" in {
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
        sessionRef <- ActiveSessionReference.createNew[Task, SyncIO].coerceTo[Task]

        createSession = factory.start[SyncIO](RemainingFlyDuration.Infinity)

        // when
        _ <- sessionRef.replaceSession(createSession.run(playerRef))
        _ <- discreteEventually {
          Task {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(RemainingFlyDuration.Infinity)
          }
        }
        _ <- sessionRef.stopAnyRunningSession

        // then
        _ <- discreteEventually {
          Task {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe NotFlying
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.minute)
    }

    "be able to replace a session" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val targetSessionLength = 10
      val targetSessionDuration = RemainingFlyDuration.PositiveMinutes.fromPositive(targetSessionLength)

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]
        sessionRef <- ActiveSessionReference.createNew[Task, SyncIO].coerceTo[Task]

        // when
        _ <- sessionRef.replaceSession(factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef))
        _ <- discreteEventually {
          Task {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(RemainingFlyDuration.Infinity)
          }
        }
        _ <- sessionRef.replaceSession(factory.start[SyncIO](targetSessionDuration).run(playerRef))

        // then
        _ <- discreteEventually {
          Task {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(targetSessionDuration)
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), targetSessionLength.minutes)
    }

    "not allow more than one session to be present" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(
          expConsumptionAmount = 0
        )

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] = playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[Task, PlayerMockReference]()

      val firstSessionLength = 10
      val firstSessionDuration = RemainingFlyDuration.PositiveMinutes.fromPositive(firstSessionLength)

      val secondSessionLength = 20
      val secondSessionDuration = RemainingFlyDuration.PositiveMinutes.fromPositive(secondSessionLength)

      assert(firstSessionLength < secondSessionLength)

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[Task]
        sessionRef <- ActiveSessionReference.createNew[Task, SyncIO].coerceTo[Task]

        // when
        _ <- sessionRef.replaceSession(factory.start[SyncIO](firstSessionDuration).run(playerRef))
        _ <- discreteEventually {
          Task {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(firstSessionDuration)
          }
        }
        _ <- sessionRef.replaceSession(factory.start[SyncIO](secondSessionDuration).run(playerRef))
        _ <- monixTimer.sleep(firstSessionLength.minutes)

        // then
        _ <- discreteEventually {
          // もしセッションが残留していた場合、飛行状態が解除されるはず
          Task {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe true
          }
        }

        // when
        _ <- monixTimer.sleep((secondSessionLength - firstSessionLength).minutes)

        // then
        _ <- discreteEventually {
          Task {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), secondSessionLength.minutes)
    }
  }

}
