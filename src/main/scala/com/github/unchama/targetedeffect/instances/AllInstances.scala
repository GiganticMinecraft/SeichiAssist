package com.github.unchama.targetedeffect.instances

import cats.Monoid
import com.github.unchama.targetedeffect.{TargetedEffect, emptyEffect}

trait TargetedEffectMonoid {
  implicit def monoid[T]: Monoid[TargetedEffect[T]] = new Monoid[TargetedEffect[T]] {
    override def empty(): TargetedEffect[T] = emptyEffect

    override def combine(a: TargetedEffect[T], b: TargetedEffect[T]): TargetedEffect[T] = {
      import com.github.unchama.generic.syntax._

      a.followedBy(b)
    }
  }
}

trait AllInstances extends TargetedEffectMonoid
