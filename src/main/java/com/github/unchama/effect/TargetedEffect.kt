package com.github.unchama.effect

import arrow.typeclasses.Monoid
import org.bukkit.command.CommandSender

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
 * 何も作用を及ぼさないような[TargetedEffect].
 */
object EmptyEffect: TargetedEffect<Any?> {
  override suspend fun runFor(minecraftObject: Any?) = Unit
}

fun String.asMessageEffect() = object : TargetedEffect<CommandSender> {
  override suspend fun runFor(minecraftObject: CommandSender) {
    minecraftObject.sendMessage(this@asMessageEffect)
  }
}

fun List<String>.asMessageEffect() = object : TargetedEffect<CommandSender> {
  override suspend fun runFor(minecraftObject: CommandSender) {
    minecraftObject.sendMessage(toTypedArray())
  }
}

operator fun <T> TargetedEffect<T>.plus(anotherEffect: TargetedEffect<T>): TargetedEffect<T> = object : TargetedEffect<T> {
  override suspend fun runFor(minecraftObject: T) {
    this@plus.runFor(minecraftObject)
    anotherEffect.runFor(minecraftObject)
  }
}
