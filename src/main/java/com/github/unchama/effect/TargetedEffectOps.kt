package com.github.unchama.effect

object TargetedEffectOps {
  operator fun <T> TargetedEffect<T>.plus(anotherEffect: TargetedEffect<T>): TargetedEffect<T> = object : TargetedEffect<T> {
    override suspend fun runFor(minecraftObject: T) {
      this@plus.runFor(minecraftObject)
      anotherEffect.runFor(minecraftObject)
    }
  }
}