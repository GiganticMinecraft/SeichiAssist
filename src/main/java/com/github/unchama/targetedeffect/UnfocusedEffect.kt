package com.github.unchama.targetedeffect

class UnfocusedEffect(val effect: suspend (Any?) -> Unit): TargetedEffect<Any?> {
  override suspend fun runFor(minecraftObject: Any?) = effect(minecraftObject)
}

/**
 * 副作用を持つ一般の[effect]を[UnfocusedEffect]として扱えるように変換する.
 */
fun unfocusedEffect(effect: suspend () -> Unit): UnfocusedEffect = UnfocusedEffect { effect() }
