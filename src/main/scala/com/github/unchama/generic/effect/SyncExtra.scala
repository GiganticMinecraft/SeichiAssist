package com.github.unchama.generic.effect

import cats.effect.Sync

object SyncExtra {
  def recoverWithStackTrace[F[_]: Sync, A](message: String, recover: A, f: F[A]): F[A] =
    Sync[F].recoverWith(f) { error =>
      Sync[F].delay {
        println(message)
        error.printStackTrace()

        recover
      }
    }
}
