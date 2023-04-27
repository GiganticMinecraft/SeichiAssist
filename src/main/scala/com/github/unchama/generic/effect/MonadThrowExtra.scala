package com.github.unchama.generic.effect

import cats.MonadError
import cats.effect.MonadThrow

object MonadThrowExtra {

  import cats.implicits._

  def retryUntilSucceeds[F[_]: MonadThrow, A](fa: F[A])(limit: Int): F[A] = {
    def go(currentIterationCount: Int, lastException: Option[Throwable]): F[A] = {
      if (currentIterationCount <= limit) {
        fa.attempt.flatMap {
          case Right(a) => a.pure[F]
          case Left(error) =>
            go(currentIterationCount + 1, Some(error)).map { a =>
              error.printStackTrace()
              a
            }
        }
      } else {
        // このelse節に入っている時点で1度は失敗しているので、`lastException`が`None`であることはありえない。
        case object LimitReached extends Exception(s"Limit $limit reached!", lastException.get)

        MonadError[F, Throwable].raiseError(LimitReached)
      }
    }

    go(0, None)
  }

}
