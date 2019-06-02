package com.github.unchama.messaging

import org.bukkit.command.CommandSender

/**
 * [CommandSender]へ送信される何らかの情報を内包しているオブジェクトへのinterface.
 */
interface MessageToSender {
  suspend fun transmitTo(commandSender: CommandSender)
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
