package com.github.unchama.generic

import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV

sealed trait TryInto[From, To, ConversionErr] {
  def tryInto(from: From): Either[ConversionErr, To]
}

object TryInto {
  implicit def refineByPredicate[A, P: Validate[A, *]]: TryInto[A, A Refined P, String] =
    from => refineV(from)

  implicit def refl[From <: To, To]: TryInto[From, To, Nothing] = from => Right(from)
}
