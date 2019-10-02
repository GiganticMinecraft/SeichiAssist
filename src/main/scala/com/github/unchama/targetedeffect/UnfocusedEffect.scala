package com.github.unchama.targetedeffect

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect

object UnfocusedEffect {
  def apply(effect: => Unit): TargetedEffect[Any] = _ => IO(effect)
}
