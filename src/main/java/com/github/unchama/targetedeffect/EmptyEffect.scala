package com.github.unchama.targetedeffect

import kotlin.coroutines.Continuation

/**
 * 何も作用を及ぼさないような[TargetedEffect].
 */
object EmptyEffect extends TargetedEffect[Any] {
  override def runFor(minecraftObject: Any, continuation: Continuation[Unit]) = Unit
}