package com.github.unchama.generic

object EitherExtra {

  def unassociate[A, B, C](eaebc: Either[A, Either[B, C]]): Either[Either[A, B], C] =
    eaebc match {
      case Left(a)         => Left(Left(a))
      case Right(Left(b))  => Left(Right(b))
      case Right(Right(c)) => Right(c)
    }

  def associate[A, B, C](eeabc: Either[Either[A, B], C]): Either[A, Either[B, C]] =
    eeabc match {
      case Left(Left(a))  => Left(a)
      case Left(Right(b)) => Right(Left(b))
      case Right(c)       => Right(Right(c))
    }

}
