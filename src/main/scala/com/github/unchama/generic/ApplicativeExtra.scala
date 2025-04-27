package com.github.unchama.generic

import cats.Applicative

object ApplicativeExtra {

  def whenAOrElse[F[_]: Applicative, A](cond: Boolean)(fa: => F[A], default: => A): F[A] =
    if (cond) fa else Applicative[F].pure(default)

  def optionOrElseA[F[_]: Applicative, A](foa1: Option[A], foa2: F[Option[A]]): F[Option[A]] =
    foa1 match {
      case Some(a) => Applicative[F].pure(Some(a))
      case None    => foa2
    }

}
