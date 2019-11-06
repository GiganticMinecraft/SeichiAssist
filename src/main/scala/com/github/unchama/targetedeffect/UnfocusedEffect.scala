package com.github.unchama.targetedeffect

import com.github.unchama.targetedeffect

object UnfocusedEffect {
  def apply(effect: => Unit): TargetedEffect[Any] = targetedeffect.delay(_ => effect)
}
