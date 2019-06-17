package com.github.unchama.targetedeffect

/**
 * 何も作用を及ぼさないような[TargetedEffect].
 */
object EmptyEffect: TargetedEffect<Any?> {
  override suspend fun runFor(minecraftObject: Any?) = Unit
}