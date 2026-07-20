package com.github.unchama.generic

import io.github.iltotore.iron.{:|, RuntimeConstraint, refineEither}

// https://www.scala-lang.org/files/archive/spec/2.13/06-expressions.html#sam-conversion
trait TryInto[From, To, ConversionErr] {
  def tryInto(from: From): Either[ConversionErr, To]
}

object TryInto {
  private def fromFunction[F, T, E](fn: F => Either[E, T]): TryInto[F, T, E] =
    (f: F) => fn(f)

  implicit def refineByConstraint[A, P](
    implicit constraint: RuntimeConstraint[A, P]
  ): TryInto[A, A :| P, String] =
    fromFunction(_.refineEither[P])

  implicit def refl[From, To >: From]: TryInto[From, To, Nothing] =
    fromFunction(from => Right(from))
}
