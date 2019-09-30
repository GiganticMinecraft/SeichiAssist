package com.github.unchama.targetedeffect

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect

/**
 * 何も作用を及ぼさないような[TargetedEffect].
 */

object EmptyEffect extends TargetedEffect[Any] {
  override def apply(v1: Any): IO[Unit] = IO.pure(Unit)
}
