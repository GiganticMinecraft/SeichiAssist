package com.github.unchama.generic.ratelimiting

import cats.effect.{SyncIO, Timer}
import com.github.unchama.generic.algebra.typeclasses.TotallyOrderedGroup
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

  implicit val intOrderedGroup: TotallyOrderedGroup[Int] = new TotallyOrderedGroup[Int] {
    override def compare(x: Int, y: Int): Int = x.compare(y)

    override def inverse(a: Int): Int = -a

    override def empty: Int = 0

    override def combine(x: Int, y: Int): Int = x + y
  }

  import cats.implicits._

  "Fixed window limiter" should {
    "block requests exceeding limits" in {
      val maxCount = 10
      val requestCount = 100

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, SyncIO, Int](maxCount, 1.minute).coerceTo[Task]
        allowances <- (1 to requestCount).toList.traverse(_ => rateLimiter.requestPermission(1)).coerceTo[Task]
      } yield {
        assert(allowances.take(maxCount).forall(_ == 1))
        assert(allowances.drop(maxCount).forall(_ == 0))
        ()
      }

      awaitForProgram(runConcurrent(program)(100), 1.seconds)
    }

    "reset back to accepting request when the specified time passes" in {
      val maxCount = 10

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, SyncIO, Int](maxCount, 1.minute).coerceTo[Task]
        _ <- (1 to maxCount).toList.traverse(_ => rateLimiter.requestPermission(1)).coerceTo[Task]
        _ <- monixTimer.sleep(1.minute + 1.second)
        allowed <- rateLimiter.requestPermission(1).coerceTo[Task].map(_ == 1)
      } yield {
        assert(allowed)
      }

      awaitForProgram(runConcurrent(program)(100), 1.minute + 1.second)
    }

    "reset back to accepting request as long as the specified time passes" in {
      val maxCount = 5
      val windowCount = 5

      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, SyncIO, Int](maxCount, 1.minute).coerceTo[Task]
        allowances <- (1 to windowCount).toList.traverse(_ =>
          (1 to maxCount)
            .toList
            .traverse(_ => rateLimiter.requestPermission(1))
            .coerceTo[Task]
            .flatTap(_ => monixTimer.sleep(1.minute + 1.second))
        )
      } yield {
        val expected = List.fill(windowCount * maxCount)(1)
        assert(allowances.flatten == expected)
        ()
      }

      awaitForProgram(runConcurrent(program)(100), windowCount.minutes + windowCount.seconds)
    }
  }

}
