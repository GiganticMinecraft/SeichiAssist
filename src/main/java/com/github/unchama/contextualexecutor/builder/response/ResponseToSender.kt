package com.github.unchama.contextualexecutor.builder.response

import org.bukkit.command.CommandSender

/**
 * コマンドの出力として[CommandSender]へ何らかの内容を送信することができるオブジェクトへのinterface
 */
interface ResponseToSender {
  suspend fun transmitTo(commandSender: CommandSender)
}

/**
 * 何も送信しないような出力
 */
object EmptyResponse: ResponseToSender {
  override suspend fun transmitTo(commandSender: CommandSender) = Unit
}

/**
 * コマンド応答を結合する.
 */
operator fun ResponseToSender.plus(anotherResponse: ResponseToSender): ResponseToSender = object : ResponseToSender {
  override suspend fun transmitTo(commandSender: CommandSender) {
    this.transmitTo(commandSender)
    anotherResponse.transmitTo(commandSender)
  }
}

fun String.asResponseToSender() = object : ResponseToSender {
  override suspend fun transmitTo(commandSender: CommandSender) {
    commandSender.sendMessage(this@asResponseToSender)
  }
}

fun List<String>.asResponseToSender() = object : ResponseToSender {
  override suspend fun transmitTo(commandSender: CommandSender) {
    commandSender.sendMessage(toTypedArray())
  }
}
