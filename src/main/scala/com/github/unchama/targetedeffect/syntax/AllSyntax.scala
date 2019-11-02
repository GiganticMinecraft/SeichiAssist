package com.github.unchama.targetedeffect.syntax

import cats.kernel.Monoid
import com.github.unchama.targetedeffect.TargetedEffect

trait TargetedEffectCombineAll {
  implicit class TargetedEffectFold[T](val effects: List[TargetedEffect[T]]) {
    import cats.implicits._

    def asSequentialEffect(): TargetedEffect[T] = Monoid[TargetedEffect[T]].combineAll(effects)
  }
}

trait AllSyntax extends TargetedEffectCombineAll
