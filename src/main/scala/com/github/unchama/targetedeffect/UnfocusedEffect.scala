package com.github.unchama.targetedeffect

object UnfocusedEffect {
  def apply(effect: => Unit): TargetedEffect[Any] = TargetedEffects.delay(_ => effect)
}
