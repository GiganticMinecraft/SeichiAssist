package com.github.unchama.generic.effect

import cats.MonadError
import cats.effect.{MonadThrow, Sync}

object MonadThrowExtra {

  import cats.implicits._

  def retryUntilSucceeds[F[_]: Sync, A](fa: F[A])(limit: Int): F[A] = {
    require(limit >= 1)
    def go(currentIterationCount: Int, occurredExceptions: List[Throwable]): F[A] = {
      if (currentIterationCount <= limit) {
        fa.attempt.flatMap {
          case Right(a) =>
            Sync[F].delay(occurredExceptions.foreach(_.printStackTrace())).as(a)
          case Left(error) =>
            go(currentIterationCount + 1, occurredExceptions :+ error)
        }
      } else {
        // このelse節に入っている時点で1度は失敗しているので、`occurredExceptions`が`empty`であることはありえない。
        case object LimitReached
            extends Exception(s"Limit $limit reached!", occurredExceptions.last)

        MonadError[F, Throwable].raiseError(LimitReached)
      }
    }

    go(0, Nil)
  }

}
