package com.github.unchama.generic.effect.concurrent

import cats.effect.{MonadThrow, Sync}

object Retry {

  import cats.implicits._

  def retryUntilSucceeds[F[_]: MonadThrow: Sync, A](fa: F[A]): F[A] = {
    fa.attempt.flatMap {
      case Right(a) => a.pure[F]
      case Left(error) =>
        Sync[F].delay(error.printStackTrace()) >> retryUntilSucceeds(fa)
    }
  }

}
