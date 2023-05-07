package com.github.unchama.generic.effect

import cats.MonadError
import cats.effect.Sync

object MonadThrowExtra {

  import cats.implicits._

  def retryUntilSucceeds[F[_]: Sync, A](fa: F[A])(limit: Int): F[A] = {
    require(limit >= 1)
    def go(currentIterationCount: Int, occurredException: Option[Throwable]): F[A] = {
      if (currentIterationCount <= limit) {
        fa.attempt.flatMap {
          case Right(a) =>
            Sync[F].delay(occurredException.foreach(_.printStackTrace())).as(a)
          case Left(error) =>
            go(currentIterationCount + 1, Some(error))
        }
      } else {
        // このelse節に入っている時点で1度は失敗しているので、`occurredExceptions`が`None`であることはありえない。
        case object LimitReached
            extends Exception(s"Limit $limit reached!", occurredException.last)

        MonadError[F, Throwable].raiseError(LimitReached)
      }
    }

    go(0, None)
  }

}
