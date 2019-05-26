package com.github.unchama.seichiassist.commands.abstract

import org.bukkit.command.CommandSender

interface ResponseToSender {
    suspend fun sendThisTo(commandSender: CommandSender)
}

fun String.asResponseToSender() = object : ResponseToSender {
    override suspend fun sendThisTo(commandSender: CommandSender) {
        commandSender.sendMessage(this)
    }
}

fun List<String>.asResponseToSender() = object : ResponseToSender {
    override suspend fun sendThisTo(commandSender: CommandSender) {
        commandSender.sendMessage(toTypedArray())
    }
}

suspend fun CommandSender.sendMessage(responseToSender: ResponseToSender) = responseToSender.sendThisTo(this)
