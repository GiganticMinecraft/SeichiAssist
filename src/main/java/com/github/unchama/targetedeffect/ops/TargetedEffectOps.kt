package com.github.unchama.targetedeffect.ops

import arrow.data.extensions.list.foldable.fold
import com.github.unchama.targetedeffect.TargetedEffect

operator fun <T> TargetedEffect<T>.plus(anotherEffect: TargetedEffect<T>): TargetedEffect<T> =
    TargetedEffect {
      this@plus.runFor(it)
      anotherEffect.runFor(it)
    }

fun <T> List<TargetedEffect<T>>.asSequentialEffect(): TargetedEffect<T> = fold(TargetedEffect.monoid())
