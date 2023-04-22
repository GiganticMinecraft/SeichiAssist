package com.github.unchama.generic.effect.concurrent

import cats.effect.MonadThrow

object Retry {

  import cats.implicits._

  def retryUntilSucceeds[F[_]: MonadThrow, A](fa: F[A]): F[A] = {
    fa.attempt.flatMap {
      case Right(a) => a.pure[F]
      case Left(error) =>
        retryUntilSucceeds(fa).map { a =>
          error.printStackTrace()
          a
        }
    }
  }

}
