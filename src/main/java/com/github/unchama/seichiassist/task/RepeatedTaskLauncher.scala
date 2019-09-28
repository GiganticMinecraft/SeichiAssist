package com.github.unchama.seichiassist.task

import java.util.concurrent.TimeUnit

import cats.effect.IO
import com.github.unchama.util.effect.IOUtils._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

abstract class RepeatedTaskLauncher {
  protected abstract val getRepeatIntervalTicks: IO[Long]

  protected abstract val runRoutine: IO[Any]

  val launch: IO[Nothing] = {
    val sleep = for {
      intervalTicks <- getRepeatIntervalTicks
      interval = FiniteDuration(intervalTicks * 50, TimeUnit.MILLISECONDS)
      _ <- IO.sleep(interval)(IO.timer(ExecutionContext.global))
    } yield ()

    val routine = for {
      _ <- sleep
      result <- runRoutine.attempt
      _ <- result match {
        case Left(error) => IO {
          println("Caught an exception while executing repeating task")
          error.printStackTrace()
        }
        case Right(value) => IO.pure(value)
      }
    } yield ()

    forever(routine)
  }
}