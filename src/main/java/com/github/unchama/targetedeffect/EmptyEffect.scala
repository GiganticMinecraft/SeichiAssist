package com.github.unchama.targetedeffect

import com.github.unchama.util.kotlin2scala.SuspendingMethod

/**
 * 何も作用を及ぼさないような[TargetedEffect].
 */
object EmptyEffect extends TargetedEffect[Any] {
  override @SuspendingMethod def runFor(minecraftObject: Any) = Unit
}