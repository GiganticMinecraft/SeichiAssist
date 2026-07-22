package com.github.unchama.testutil.concurrent.tests

import cats.Monad
import cats.effect.IO
import org.scalactic.source.Position
import org.scalatest.exceptions.{StackDepthException, TestFailedException}

trait IODiscreteEventually {

  case class DiscreteEventuallyConfig(trialCount: Int)

  implicit val discreteEventuallyConfig: DiscreteEventuallyConfig = DiscreteEventuallyConfig(
    1000
  )

  def discreteEventually[T](
    action: IO[T]
  )(implicit config: DiscreteEventuallyConfig, pos: Position): IO[T] = {
    Monad[IO].tailRecM[Int, T](1) { attemptCount =>
      action.map(Right(_)).handleErrorWith { error =>
        if (attemptCount < config.trialCount) {
          IO.cede.as(Left(attemptCount + 1))
        } else {
          val testFailure = new TestFailedException(
            (_: StackDepthException) => {
              val message =
                if (error.getMessage == null)
                  s"Failed after $attemptCount trials."
                else
                  s"Failed after $attemptCount trials. Last failure: ${error.getMessage}"

              Some(message)
            },
            Some(error),
            Left(pos),
            None,
            Vector.empty
          )

          IO.raiseError(testFailure)
        }
      }
    }
  }
}

object IODiscreteEventually extends IODiscreteEventually
