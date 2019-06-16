package com.github.unchama.effect

import org.bukkit.command.CommandSender

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