package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

object MessageEffect {
  def apply(string: String): TargetedEffect[CommandSender] =
    targetedeffect.delay(_.sendMessage(string))

  def apply(stringList: List[String]): TargetedEffect[CommandSender] =
    targetedeffect.delay(_.sendMessage(stringList.toArray))
}
