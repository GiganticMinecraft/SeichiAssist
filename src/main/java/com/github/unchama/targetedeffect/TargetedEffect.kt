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

/**
 * 副作用を持つ[effect]を[TargetedEffect]として扱えるように変換する.
 */
fun asTargeted(effect: suspend () -> Unit): TargetedEffect<Any?> = object : TargetedEffect<Any?> {
  override suspend fun runFor(minecraftObject: Any?) = effect.invoke()
}

/**
 * [TargetedEffect]を計算する非純粋な関数[f]を[TargetedEffect]として扱えるように変換する.
 */
fun <T> computedEffect(f: suspend () -> TargetedEffect<T>): TargetedEffect<T> = object : TargetedEffect<T> {
  override suspend fun runFor(minecraftObject: T) = f().runFor(minecraftObject)
}
