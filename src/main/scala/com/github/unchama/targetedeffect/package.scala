package com.github.unchama

import cats.data.Kleisli
import cats.effect.IO

package object targetedeffect {

  type TargetedEffect[-T] = Kleisli[IO, T, Unit]
  type TargetedEffectF[F[_], -T] = Kleisli[F, T, Unit]

}
