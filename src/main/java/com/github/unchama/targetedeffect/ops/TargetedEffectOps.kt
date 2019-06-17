package com.github.unchama.targetedeffect.ops

import arrow.data.extensions.list.foldable.fold
import com.github.unchama.targetedeffect.TargetedEffect

operator fun <T> TargetedEffect<T>.plus(anotherEffect: TargetedEffect<T>): TargetedEffect<T> = object : TargetedEffect<T> {
  override suspend fun runFor(minecraftObject: T) {
    this@plus.runFor(minecraftObject)
    anotherEffect.runFor(minecraftObject)
  }
}

fun <T> List<TargetedEffect<T>>.asSequentialEffect(): TargetedEffect<T> = fold(TargetedEffect.monoid())
