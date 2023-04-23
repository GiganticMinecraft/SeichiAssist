package com.github.unchama.generic.effect

import cats.MonadError
import cats.effect.MonadThrow

object MonadThrowExtra {

  import cats.implicits._

  def retryUntilSucceeds[F[_]: MonadThrow, A](fa: F[A])(limit: Int, now: Int = 0): F[A] = {
    if (now <= limit) {
      fa.attempt.flatMap {
        case Right(a) => a.pure[F]
        case Left(error) =>
          retryUntilSucceeds(fa)(limit, now + 1).map { a =>
            error.printStackTrace()
            a
          }
      }
    } else {
      case object LimitReached extends Exception(s"Limit $limit reached!")

      MonadError[F, Throwable].raiseError(LimitReached)
    }
  }

}
