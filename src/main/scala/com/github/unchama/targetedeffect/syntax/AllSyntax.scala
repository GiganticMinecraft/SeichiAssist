package com.github.unchama.targetedeffect.syntax

import com.github.unchama.targetedeffect.{TargetedEffect, instances}

trait TargetedEffectCombineAll {
  implicit class TargetedEffectFold[T](val effects: List[TargetedEffect[T]]) {
    def asSequentialEffect(): TargetedEffect[T] = instances.monoid[T].combineAll(effects)
  }
}

trait AllSyntax extends TargetedEffectCombineAll
