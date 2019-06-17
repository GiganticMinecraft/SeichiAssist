package com.github.unchama.targetedeffect

import org.bukkit.command.CommandSender

fun String.asMessageEffect() =
    TargetedEffect { commandSender: CommandSender ->
      commandSender.sendMessage(this@asMessageEffect)
    }

fun List<String>.asMessageEffect() =
  TargetedEffect { commandSender: CommandSender ->
    commandSender.sendMessage(this@asMessageEffect.toTypedArray())
  }
