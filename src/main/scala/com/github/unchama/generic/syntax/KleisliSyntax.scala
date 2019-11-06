package com.github.unchama.generic.syntax

import cats.Apply
import cats.data.Kleisli

trait KleisliSyntax {
  implicit class KleisliCombine[F[_]: Apply, A, B](val effect: Kleisli[F, A, B]) {
    def followedBy[AA <: A](anotherEffect: Kleisli[F, AA, B]): Kleisli[F, AA, B] = {
      import cats.implicits._
      Kleisli(aa => effect(aa) *> anotherEffect(aa))
    }
  }
}
