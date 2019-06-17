package com.github.unchama.effect.ops

import arrow.data.extensions.list.foldable.fold
import com.github.unchama.effect.TargetedEffect

operator fun <T> TargetedEffect<T>.plus(anotherEffect: TargetedEffect<T>): TargetedEffect<T> = object : TargetedEffect<T> {
  override suspend fun runFor(minecraftObject: T) {
    this@plus.runFor(minecraftObject)
    anotherEffect.runFor(minecraftObject)
  }
}

fun <T> List<TargetedEffect<T>>.combineAll(): TargetedEffect<T> = fold(TargetedEffect.monoid())
