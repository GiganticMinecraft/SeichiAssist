package com.github.unchama.generic.effect.stream

import cats.effect.SyncIO
import com.github.unchama.generic.Token
import com.github.unchama.generic.effect.stream.ReorderingPipe.TimeStamped
import com.github.unchama.testutil.concurrent.tests.ConcurrentEffectTest
import com.github.unchama.testutil.execution.MonixTestSchedulerTests
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.util.Random

class ReorderingPipeSpec
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

  "ReorderingPipe" should {
    type TestInputType = Long

    "Reorder scrambled inputs as long as they are timestamped" in {
      forAll(minSuccessful(100)) { input: List[TestInputType] =>
        whenever(input.nonEmpty) {
          val timeStamped: Vector[TimeStamped[TestInputType]] = {
            val withCurrentStamps = input.map(input => (new Token, input))

            val last = {
              val (lastToken, lastInput) = withCurrentStamps.last
              TimeStamped(lastToken, new Token, lastInput)
            }

            val otherThanLast = withCurrentStamps.sliding(2).flatMap {
              case (currentToken, currentInput) :: (nextToken, _) :: _ =>
                Some(TimeStamped(currentToken, nextToken, currentInput))
              case _ =>
                None
            }

            otherThanLast.toVector.appended(last)
          }

          val createRandomizedInput: SyncIO[Vector[TimeStamped[TestInputType]]] = SyncIO {
            Random.shuffle(timeStamped)
          }

          val program =
            fs2
              .Stream
              .evals(createRandomizedInput)
              .through(
                ReorderingPipe
                  .withInitialToken[SyncIO, TestInputType](timeStamped.head.currentStamp)
              )
              .compile
              .toList

          assertResult(input)(program.unsafeRunSync())
        }
      }
    }
  }
}
