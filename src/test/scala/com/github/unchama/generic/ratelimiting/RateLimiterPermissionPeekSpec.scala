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
    "not modify available permission when peaked" in {
      val program = for {
        rateLimiter <- FixedWindowRateLimiter.in[Task, SyncIO, Natural](100, 10.seconds).coerceTo[Task]
        peek1 <- rateLimiter.peekAvailablePermission.coerceTo[Task]
        peek2 <- rateLimiter.peekAvailablePermission.coerceTo[Task]
      } yield peek1 == peek2

      assert(awaitForProgram(runConcurrent(program)(100), 1.second).forall(a => a))
    }
  }
}
