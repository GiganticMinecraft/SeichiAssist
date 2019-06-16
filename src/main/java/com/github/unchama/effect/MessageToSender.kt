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
}

/**
 * 何も作用を及ぼさないような[TargetedEffect].
 */
object EmptyEffect: TargetedEffect<Any?> {
  override suspend fun runFor(minecraftObject: Any?) = Unit
}

/**
 * [CommandSender]へ送信される何らかの情報を内包しているオブジェクトへのinterface.
 */
interface MessageToSender {
  suspend fun transmitTo(commandSender: CommandSender)

  companion object {
    val monoid = object : Monoid<MessageToSender> {
      override fun empty() = EmptyMessage
      override fun MessageToSender.combine(b: MessageToSender) = this + b
    }
  }
}

/**
 * 何も送信しないような出力.
 */
object EmptyMessage: MessageToSender {
  override suspend fun transmitTo(commandSender: CommandSender) = Unit
}

/**
 * 送信の操作が結合された新しい[MessageToSender]を作成する.
 */
operator fun MessageToSender.plus(anotherMessage: MessageToSender): MessageToSender = object : MessageToSender {
  override suspend fun transmitTo(commandSender: CommandSender) {
    this.transmitTo(commandSender)
    anotherMessage.transmitTo(commandSender)
  }
}

fun String.asResponseToSender() = object : MessageToSender {
  override suspend fun transmitTo(commandSender: CommandSender) {
    commandSender.sendMessage(this@asResponseToSender)
  }
}

fun List<String>.asResponseToSender() = object : MessageToSender {
  override suspend fun transmitTo(commandSender: CommandSender) {
    commandSender.sendMessage(toTypedArray())
  }
}
