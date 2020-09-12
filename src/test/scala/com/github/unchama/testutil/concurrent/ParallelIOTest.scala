package com.github.unchama.testutil.concurrent

import cats.effect.{ContextShift, IO}

trait ParallelIOTest {

  import cats.implicits._

  def runParallel[R](parallelism: Int)(program: IO[R])(implicit shift: ContextShift[IO]): IO[List[R]] = {
    for {
      startedFibers <- List.fill(parallelism)(program)
        .map(_.start)
        .sequence

      results <- startedFibers.map(_.join).sequence
    } yield results
  }

}

object ParallelIOTest extends ParallelIOTest
