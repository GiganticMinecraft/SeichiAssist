package com.github.unchama.generic.ratelimiting

import cats.effect.Timer
import com.github.unchama.testutil.concurrent.tests.TaskDiscreteEventually
import io.github.iltotore.iron.constraint.numeric.GreaterEqual
import io.github.iltotore.iron.refineUnsafe
import monix.catnap.SchedulerEffect
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.util.Random

class FixedWindowRateLimiterSpec
    extends GenericRateLimiterSpec
    with ScalaCheckPropertyChecks
    with TaskDiscreteEventually {

  import cats.implicits._

  import scala.concurrent.duration._

  override def newRandomRateLimiter(
    seed: Int
  )(implicit monixTimer: Timer[Task]): Task[RateLimiter[Task, Natural]] = {
    val random = new Random(seed)
    val maxPermit = (random.nextInt(1000).refineUnsafe[GreaterEqual[0]])
    val sleepTime = random.nextInt(60000).millis

    FixedWindowRateLimiter.in[Task, Natural](maxPermit, sleepTime)
  }

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 10.millis)
  implicit override val discreteEventuallyConfig: DiscreteEventuallyConfig =
    DiscreteEventuallyConfig(10000)

  implicit val monixScheduler: TestScheduler = TestScheduler(
    ExecutionModel.SynchronousExecution
  )
  implicit val monixTimer: Timer[Task] = SchedulerEffect.timer(monixScheduler)

  "Fixed window limiter" should {
    keepPermitsEqual()

    "block requests exceeding limits" in {
      val maxCount: Natural = (10).refineUnsafe[GreaterEqual[0]]
      val requestCount: Natural = (100).refineUnsafe[GreaterEqual[0]]

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, Natural](maxCount, 1.minute)
        allowances <- (1 to requestCount)
          .toList
          .traverse(_ => rateLimiter.requestPermission((1).refineUnsafe[GreaterEqual[0]]))
      } yield {
        assert(allowances.take(maxCount).forall(_ == (1).refineUnsafe[GreaterEqual[0]]))
        assert(allowances.drop(maxCount).forall(_ == (0).refineUnsafe[GreaterEqual[0]]))
        ()
      }

      awaitForProgram(runConcurrent(program)(100), 1.seconds)
    }

    "reset back to accepting request when the specified time passes" in {
      val maxCount: Natural = (10).refineUnsafe[GreaterEqual[0]]

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, Natural](maxCount, 1.minute)
        _ <- (1 to maxCount)
          .toList
          .traverse(_ => rateLimiter.requestPermission((1).refineUnsafe[GreaterEqual[0]]))
        _ <- monixTimer.sleep(1.minute + 1.second)
        allowed <- rateLimiter
          .requestPermission((1).refineUnsafe[GreaterEqual[0]])
          .map(_ == (1).refineUnsafe[GreaterEqual[0]])
      } yield {
        assert(allowed)
      }

      awaitForProgram(runConcurrent(program)(100), 1.minute + 1.second)
    }

    "reset back to accepting request as long as the specified time passes" in {
      val maxCount: Natural = (5).refineUnsafe[GreaterEqual[0]]
      val windowCount = 5

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, Natural](maxCount, 1.minute)
        allowances <- (1 to windowCount)
          .toList
          .traverse(_ =>
            (1 to maxCount)
              .toList
              .traverse(_ => rateLimiter.requestPermission((1).refineUnsafe[GreaterEqual[0]]))
              .flatTap(_ => monixTimer.sleep(1.minute + 1.second))
          )
      } yield {
        val expected =
          List.fill(windowCount * maxCount)((1).refineUnsafe[GreaterEqual[0]]: Natural)
        assert(allowances.flatten == expected)
        ()
      }

      awaitForProgram(runConcurrent(program)(100), windowCount.minutes + windowCount.seconds)
    }
  }
}
