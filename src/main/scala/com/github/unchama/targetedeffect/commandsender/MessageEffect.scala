package com.github.unchama.targetedeffect.commandsender

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

object MessageEffect {
  def apply(string: String): TargetedEffect[CommandSender] =
    TargetedEffect.delay(_.sendMessage(string))

  def apply(stringList: List[String]): TargetedEffect[CommandSender] =
    TargetedEffect.delay(_.sendMessage(stringList.toArray))
}
