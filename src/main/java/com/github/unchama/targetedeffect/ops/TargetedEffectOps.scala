package com.github.unchama.targetedeffect.ops

import com.github.unchama.targetedeffect.TargetedEffect

import scala.collection.JavaConverters._

object TargetedEffectOps {
    implicit class TargetedEffectCombine[T](val effect: TargetedEffect[T]) {
        def +(anotherEffect: TargetedEffect[T]): TargetedEffect[T] =
            TargetedEffect.monoid[T]().combine(effect, anotherEffect)
    }

    implicit class TargetedEffectFold[T](val effects: List[TargetedEffect[T]]) {
        def asSequentialEffect(): TargetedEffect[T] = TargetedEffect.monoid[T]().combineAll(effects.asJava)
    }
}