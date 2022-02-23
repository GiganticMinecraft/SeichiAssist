package com.github.unchama.generic.ratelimiting

import cats.effect.{SyncIO, Timer}
import cats.implicits._
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.generic.ContextCoercion._
import com.github.unchama.testutil.concurrent.tests.{ConcurrentEffectTest, TaskDiscreteEventually}
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.refineV
import eu.timepit.refined.auto._
import monix.catnap.SchedulerEffect
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.Span
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class RateLimiterPermissionPeekSpec extends AnyWordSpec
  with ScalaCheckPropertyChecks
  with Matchers
  with TaskDiscreteEventually
  with ConcurrentEffectTest
  with MonixTestSchedulerTests {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 10.millis)
  implicit override val discreteEventuallyConfig: DiscreteEventuallyConfig = DiscreteEventuallyConfig(10000)

  implicit val monixScheduler: TestScheduler = TestScheduler(ExecutionModel.SynchronousExecution)
  implicit val monixTimer: Timer[Task] = SchedulerEffect.timer(monixScheduler)

  type Natural = Int Refined NonNegative

  implicit val intOrderedMonus: OrderedMonus[Natural] = new OrderedMonus[Natural] {
    override def |-|(x: Natural, y: Natural): Natural =
      if (x >= y) refineV[NonNegative](x - y).getOrElse(throw new RuntimeException)
      else 0

    override def empty: Natural = 0

    override def combine(x: Natural, y: Natural): Natural =
      refineV[NonNegative](x + y).getOrElse(throw new RuntimeException)

    override def compare(x: Natural, y: Natural): Int = x.value.compare(y.value)
  }

  "RateLimiter" should {
    "keep equality of permits with another RateLimiter which has not been peeked" in {
      val maxPermits: Natural = 100
      val sleepPeriod = 5.seconds
      // any2stringadd!!! :rage:
      val period = sleepPeriod plus 5.seconds
      val program = for {
        rateLimiterA <- FixedWindowRateLimiter.in[Task, Task, Natural](maxPermits, period)
        rateLimiterB <- FixedWindowRateLimiter.in[Task, Task, Natural](maxPermits, period)
        _ <- rateLimiterA.peekAvailablePermissions
        _ <- monixTimer.sleep(sleepPeriod)
        peekA <- rateLimiterA.peekAvailablePermissions
        peekB <- rateLimiterB.peekAvailablePermissions
      } yield {
        assert(peekA == peekB)
        ()
      }

      awaitForProgram(runConcurrent(program)(100), sleepPeriod)
    }
  }
}
