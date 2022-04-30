package com.github.unchama.testutil.concurrent.tests

import cats.effect.Concurrent

trait ConcurrentEffectTest {

  import cats.effect.implicits._
  import cats.implicits._

  def runConcurrent[F[_]: Concurrent, R](program: F[R])(concurrency: Int): F[List[R]] = {
    for {
      startedFibers <- List.fill(concurrency)(program).traverse(_.start)

      results <- startedFibers.traverse(_.join)
    } yield results
  }

}

object ConcurrentEffectTest extends ConcurrentEffectTest
