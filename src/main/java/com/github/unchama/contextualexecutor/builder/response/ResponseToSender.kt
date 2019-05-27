package com.github.unchama.contextualexecutor.builder.response

import org.bukkit.command.CommandSender

/**
 * コマンドの出力として[CommandSender]へ何らかの内容を送信することができるオブジェクトへのinterface
 */
interface ResponseToSender {
    suspend fun transmitTo(commandSender: CommandSender)
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
