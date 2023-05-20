package com.github.unchama.generic.effect.concurrent

import cats.effect.{ContextShift, IO, Timer}
import com.github.unchama.testutil.concurrent.tests.ConcurrentEffectTest
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext

class AsymmetricSignallingRefSpec
    extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers
    with ConcurrentEffectTest
    with MonixTestSchedulerTests {

  import scala.concurrent.duration._

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 10.millis)
  implicit val monixScheduler: TestScheduler = TestScheduler(
    ExecutionModel.AlwaysAsyncExecution
  )
  implicit val monixTimer: Timer[Task] = Task.timer(monixScheduler)

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  type Value = Int

  "AsymmetricSignallingRef" should {
    import cats.implicits._

    "signal all the changes" in {
      val initialValue: Value = 0

      forAll(minSuccessful(10000)) { updates: List[Value] =>
        val task = for {
          ref <- AsymmetricSignallingRef.in[Task, Task, Task, Value](initialValue)
          updateResult <-
            ref.valuesAwait.use { stream =>
              for {
                resultFiber <- stream.take(updates.length).compile.toList.start
                _ <- updates.traverse(ref.set)
                result <- resultFiber.join
              } yield result
            }
        } yield updateResult

        assertResult(updates)(awaitForProgram(task, 1.second))
      }
    }
  }
}
