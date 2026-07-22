package com.github.unchama.seichiassist.subsystems.managedfly.application

import cats.effect.{IO, SyncIO}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{
  Flying,
  NotFlying,
  RemainingFlyDuration
}
import com.github.unchama.testutil.concurrent.tests.{ConcurrentEffectTest, IODiscreteEventually}
import com.github.unchama.testutil.execution.CatsEffectTestControl
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ActiveSessionReferenceSpec
    extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers
    with IODiscreteEventually
    with ConcurrentEffectTest
    with CatsEffectTestControl {

  import com.github.unchama.generic.ContextCoercion._

  import scala.concurrent.duration._

  implicit override val discreteEventuallyConfig: DiscreteEventuallyConfig =
    DiscreteEventuallyConfig(1000000)

  val mock = new Mock[IO, SyncIO]

  import mock._

  "New fly session reference" should {
    "not have started any session" in {
      val program = for {
        sessionRef <- ActiveSessionReference.createNew[IO, SyncIO].coerceTo[IO]
        status <- sessionRef.getLatestFlyStatus.coerceTo[IO]
      } yield status

      awaitForProgram(program) shouldBe NotFlying
    }
  }

  "Active fly session reference" should {
    "correctly expose the fly status of a started session" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(expConsumptionAmount = 0)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] =
        playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val sessionLengthInMinutes = 10
      val sessionDuration =
        RemainingFlyDuration.PositiveMinutes.fromPositive(sessionLengthInMinutes)

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[IO]
        sessionRef <- ActiveSessionReference.createNew[IO, SyncIO].coerceTo[IO]

        createSession = factory.start[SyncIO](sessionDuration)

        // when
        _ <- sessionRef.replaceSession(createSession.run(playerRef))

        // then
        _ <- discreteEventually {
          IO {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(sessionDuration)
          }
        }

        // when
        _ <- IO.sleep(sessionLengthInMinutes.minutes)

        // then
        _ <- discreteEventually {
          IO {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe NotFlying
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), sessionLengthInMinutes.minutes)
    }

    "be able to stop a running session" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(expConsumptionAmount = 0)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] =
        playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[IO]
        sessionRef <- ActiveSessionReference.createNew[IO, SyncIO].coerceTo[IO]

        createSession = factory.start[SyncIO](RemainingFlyDuration.Infinity)

        // when
        _ <- sessionRef.replaceSession(createSession.run(playerRef))
        _ <- discreteEventually {
          IO {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(
              RemainingFlyDuration.Infinity
            )
          }
        }
        _ <- sessionRef.stopAnyRunningSession

        // then
        _ <- discreteEventually {
          IO {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe NotFlying
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.minute)
    }

    "be able to replace a session" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(expConsumptionAmount = 0)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] =
        playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val targetSessionLength = 10
      val targetSessionDuration =
        RemainingFlyDuration.PositiveMinutes.fromPositive(targetSessionLength)

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[IO]
        sessionRef <- ActiveSessionReference.createNew[IO, SyncIO].coerceTo[IO]

        // when
        _ <- sessionRef.replaceSession(
          factory.start[SyncIO](RemainingFlyDuration.Infinity).run(playerRef)
        )
        _ <- discreteEventually {
          IO {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(
              RemainingFlyDuration.Infinity
            )
          }
        }
        _ <- sessionRef.replaceSession(
          factory.start[SyncIO](targetSessionDuration).run(playerRef)
        )

        // then
        _ <- discreteEventually {
          IO {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(targetSessionDuration)
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), targetSessionLength.minutes)
    }

    "not allow more than one session to be present" in {
      // given
      implicit val configuration: SystemConfiguration =
        SystemConfiguration(expConsumptionAmount = 0)

      implicit val manipulationMock: PlayerFlyStatusManipulation[PlayerAsyncKleisli] =
        playerMockFlyStatusManipulation
      val factory = new ActiveSessionFactory[IO, PlayerMockReference]()

      val firstSessionLength = 10
      val firstSessionDuration =
        RemainingFlyDuration.PositiveMinutes.fromPositive(firstSessionLength)

      val secondSessionLength = 20
      val secondSessionDuration =
        RemainingFlyDuration.PositiveMinutes.fromPositive(secondSessionLength)

      assert(firstSessionLength < secondSessionLength)

      val program = for {
        // given
        playerRef <- PlayerMockReference(
          initiallyFlying = false,
          InfiniteExperience,
          initiallyIdle = false
        ).coerceTo[IO]
        sessionRef <- ActiveSessionReference.createNew[IO, SyncIO].coerceTo[IO]

        // when
        _ <- sessionRef.replaceSession(
          factory.start[SyncIO](firstSessionDuration).run(playerRef)
        )
        _ <- discreteEventually {
          IO {
            sessionRef.getLatestFlyStatus.unsafeRunSync() shouldBe Flying(firstSessionDuration)
          }
        }
        _ <- sessionRef.replaceSession(
          factory.start[SyncIO](secondSessionDuration).run(playerRef)
        )
        _ <- IO.sleep(firstSessionLength.minutes)

        // then
        _ <- discreteEventually {
          // もしセッションが残留していた場合、飛行状態が解除されるはず
          IO {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe true
          }
        }

        // when
        _ <- IO.sleep((secondSessionLength - firstSessionLength).minutes)

        // then
        _ <- discreteEventually {
          IO {
            playerRef.isFlyingMutex.readLatest.unsafeRunSync() shouldBe false
          }
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), secondSessionLength.minutes)
    }
  }

}
