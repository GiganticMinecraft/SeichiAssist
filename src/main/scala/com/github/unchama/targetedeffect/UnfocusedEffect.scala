package com.github.unchama.targetedeffect

import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect

object UnfocusedEffect {
  def apply(effect: => Unit): TargetedEffect[Any] = TargetedEffects.delay(_ => effect)
}
