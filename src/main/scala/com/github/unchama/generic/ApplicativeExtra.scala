package com.github.unchama.generic

import cats.Applicative

object ApplicativeExtra {

  def whenAOrElse[F[_]: Applicative, A](cond: Boolean)(fa: F[A], default: => A): F[A] =
    if (cond) fa else Applicative[F].pure(default)

}
