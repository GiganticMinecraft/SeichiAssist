package com.github.unchama.targetedeffect

def String.asMessageEffect() =
    TargetedEffect { commandSender: CommandSender ->
      commandSender.sendMessage(this@asMessageEffect)
    }

def List<String>.asMessageEffect() =
  TargetedEffect { commandSender: CommandSender ->
    commandSender.sendMessage(this@asMessageEffect.toTypedArray())
  }
