package com.github.unchama.testutil.concurrent.tests

import cats.effect.{Concurrent, ContextShift}

trait ParallelEffectTest {

  import cats.effect.implicits._
  import cats.implicits._

  def runParallel[F[_] : Concurrent, R](program: F[R])(parallelism: Int)(implicit shift: ContextShift[F]): F[List[R]] = {
    for {
      startedFibers <- List.fill(parallelism)(program)
        .map(_.start)
        .sequence

      results <- startedFibers.map(_.join).sequence
    } yield results
  }

}

object ParallelEffectTest extends ParallelEffectTest
