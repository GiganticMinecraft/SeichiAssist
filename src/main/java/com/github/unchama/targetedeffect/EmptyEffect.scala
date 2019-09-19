package com.github.unchama.targetedeffect

/**
 * 何も作用を及ぼさないような[TargetedEffect].
 */
object EmptyEffect: TargetedEffect<Any?> {
  override suspend def runFor(minecraftObject: Any?) = Unit
}