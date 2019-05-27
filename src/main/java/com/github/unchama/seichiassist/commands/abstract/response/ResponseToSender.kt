package com.github.unchama.seichiassist.commands.abstract.response

import org.bukkit.command.CommandSender

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
