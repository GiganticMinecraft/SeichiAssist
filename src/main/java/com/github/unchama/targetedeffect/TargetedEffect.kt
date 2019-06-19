package com.github.unchama.targetedeffect

import arrow.typeclasses.Monoid
import com.github.unchama.targetedeffect.ops.asSequentialEffect

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

    operator fun <T> invoke(effect: suspend (T) -> Unit): TargetedEffect<T> = object : TargetedEffect<T> {
      override suspend fun runFor(minecraftObject: T) = effect(minecraftObject)
    }
  }
}

/**
 * [TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
 */
fun <T> deferredEffect(f: suspend () -> TargetedEffect<T>): TargetedEffect<T> = TargetedEffect { f().runFor(it) }

/**
 * 実行対象の[T]から[TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
 */
fun <T> computedEffect(f: suspend (T) -> TargetedEffect<T>): TargetedEffect<T> = TargetedEffect { f(it).runFor(it) }

fun <T> sequentialEffect(vararg effects: TargetedEffect<T>): TargetedEffect<T> = effects.toList().asSequentialEffect()
