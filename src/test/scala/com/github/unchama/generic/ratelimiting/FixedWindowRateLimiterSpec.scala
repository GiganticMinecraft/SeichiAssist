package com.github.unchama.generic.ratelimiting

import cats.effect.{SyncIO, Timer}
import com.github.unchama.testutil.concurrent.tests.{ConcurrentEffectTest, TaskDiscreteEventually}
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import monix.catnap.SchedulerEffect
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FixedWindowRateLimiterSpec
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

  implicit val monixScheduler: TestScheduler = TestScheduler(ExecutionModel.SynchronousExecution)
  implicit val monixTimer: Timer[Task] = SchedulerEffect.timer(monixScheduler)

  import cats.implicits._

  "Fixed window limiter" should {
    "block requests exceeding limits" in {
      val maxCount = 10
      val requestCount = 100

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, SyncIO](maxCount, 1.minute).coerceTo[Task]
        allowances <- (1 to requestCount).toList.traverse(_ => rateLimiter.requestPermission).coerceTo[Task]
      } yield {
        assert(allowances.take(maxCount).forall(allowed => allowed))
        assert(allowances.drop(maxCount).forall(allowed => !allowed))
        ()
      }

      awaitForProgram(runConcurrent(program)(100), 1.seconds)
    }

    "reset back to accepting request when the specified time passes" in {
      val maxCount = 10

      // TODO this must not pass

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, SyncIO](maxCount, 1.minute).coerceTo[Task]
        _ <- (1 to maxCount).toList.traverse(_ => rateLimiter.requestPermission).coerceTo[Task]
        _ <- discreteEventually {
          rateLimiter.requestPermission.coerceTo[Task]
        }
      } yield ()

      awaitForProgram(runConcurrent(program)(100), 1.seconds)
    }

    "reset back to accepting request as long as the specified time passes" in {
      val maxCount = 5
      val windowCount = 5

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, SyncIO](maxCount, 1.minute).coerceTo[Task]
        allowances <- (1 to windowCount).toList.traverse(_ =>
          (1 to maxCount)
            .toList
            .traverse(_ => rateLimiter.requestPermission)
            .coerceTo[Task]
            .flatTap(_ => monixTimer.sleep(1.minute + 1.second))
        )
      } yield {
        val expected = List.fill(windowCount * maxCount)(true)
        assert(allowances.flatten == expected)
        ()
      }

      awaitForProgram(runConcurrent(program)(100), windowCount.minutes + windowCount.seconds)
    }
  }

}
