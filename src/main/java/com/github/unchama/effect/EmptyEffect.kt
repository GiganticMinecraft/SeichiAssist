package com.github.unchama.effect

/**
 * 何も作用を及ぼさないような[TargetedEffect].
 */
object EmptyEffect: TargetedEffect<Any?> {
  override suspend fun runFor(minecraftObject: Any?) = Unit
}