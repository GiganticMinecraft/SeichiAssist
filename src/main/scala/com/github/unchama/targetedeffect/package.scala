package com.github.unchama

import cats.data.Kleisli
import cats.effect.IO

package object targetedeffect extends TargetedEffectFactory {

  type TargetedEffect[-T] = Kleisli[IO, T, Unit]

}
