package com.github.unchama.testutil.concurrent.tests

import cats.Monad
import monix.eval.Task
import org.scalactic.source.Position
import org.scalatest.exceptions.{StackDepthException, TestFailedException}

trait TaskDiscreteEventually {

  case class DiscreteEventuallyConfig(trialCount: Int)

  implicit val discreteEventuallyConfig: DiscreteEventuallyConfig = DiscreteEventuallyConfig(1000)

  def discreteEventually[T](task: Task[T])(implicit config: DiscreteEventuallyConfig, pos: Position): Task[T] = {
    for {
      taskResult <- Monad[Task].tailRecM[Int, T](1) { attemptCount =>
        for {
          // try completing the task, and sleep or exit if failed
          trialResult <- task.map(Right(_)).onErrorHandleWith { e =>
            if (attemptCount < config.trialCount) {
              Task.shift >> Task.pure(Left(attemptCount + 1))
            } else {
              val error = {
                new TestFailedException(
                  (_: StackDepthException) => {
                    val message =
                      if (e.getMessage == null)
                        s"Failed after $attemptCount trials."
                      else
                        s"Failed after $attemptCount trials. Last failure: ${e.getMessage}"

                    Some(message)
                  },
                  Some(e),
                  Left(pos),
                  None,
                  Vector.empty
                )
              }

              Task.raiseError(error)
            }
          }
        } yield trialResult
      }
    } yield taskResult
  }
}

object TaskDiscreteEventually extends TaskDiscreteEventually
