package com.github.unchama.generic

import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV

// https://www.scala-lang.org/files/archive/spec/2.13/06-expressions.html#sam-conversion
trait TryInto[From, To, ConversionErr] {
  def tryInto(from: From): Either[ConversionErr, To]
}

object TryInto {
  private def fromFunction[F, T, E](fn: F => Either[E, T]): TryInto[F, T, E] =
    (f: F) => fn(f)

  implicit def refineByPredicate[A, P: Validate[A, *]]: TryInto[A, A Refined P, String] =
    fromFunction(refineV(_))

  implicit def refl[From <: To, To]: TryInto[From, To, Nothing] =
    fromFunction(from => Right(from))
}
