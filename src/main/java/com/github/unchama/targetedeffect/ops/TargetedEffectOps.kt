package com.github.unchama.targetedeffect.ops

import arrow.data.extensions.list.foldable.fold
import com.github.unchama.targetedeffect.TargetedEffect

operator fun <T> TargetedEffect<T>.plus(anotherEffect: TargetedEffect<T>): TargetedEffect<T> =
    with (TargetedEffect.monoid<T>()) { this@plus.combine(anotherEffect) }

fun <T> List<TargetedEffect<T>>.asSequentialEffect(): TargetedEffect<T> = fold(TargetedEffect.monoid())
