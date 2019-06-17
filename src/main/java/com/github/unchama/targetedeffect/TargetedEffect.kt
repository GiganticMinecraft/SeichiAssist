package com.github.unchama.targetedeffect

import arrow.typeclasses.Monoid

/**
 * Minecraft内の何らかの対象[T]に向けた作用を持ち,
 * [runFor]メソッドにより作用を[T]に及ぼすことができるオブジェクトへのinterface.
 *
 * [runFor]の副作用は, Arrow Fxの設計理念に従いコルーチンの中で発動される.
 */
interface TargetedEffect<in T>{
  suspend fun runFor(minecraftObject: T)

  companion object {
    fun <T> monoid(): Monoid<TargetedEffect<T>> = object : Monoid<TargetedEffect<T>> {
      override fun empty(): TargetedEffect<T> = EmptyEffect
      override fun TargetedEffect<T>.combine(b: TargetedEffect<T>): TargetedEffect<T> = this + b
    }
  }
}

