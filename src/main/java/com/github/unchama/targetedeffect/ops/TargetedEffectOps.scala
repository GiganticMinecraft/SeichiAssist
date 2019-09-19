package com.github.unchama.targetedeffect.ops

operator def <T> TargetedEffect<T>.plus(anotherEffect: TargetedEffect<T>): TargetedEffect<T> =
    with (TargetedEffect.monoid<T>()) { this@plus.combine(anotherEffect) }

def <T> List<TargetedEffect<T>>.asSequentialEffect(): TargetedEffect<T> = fold(TargetedEffect.monoid())
