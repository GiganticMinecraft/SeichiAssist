package com.github.unchama.targetedeffect

import org.bukkit.command.CommandSender

object MessageEffects {

  implicit class StringMessageEffect(val string: String) {
    def asMessageEffect(): TargetedEffect[CommandSender] =
      TargetedEffects.delay(_.sendMessage(string))
  }

  implicit class StringListMessageEffect(val stringList: List[String]) {
    def asMessageEffect(): TargetedEffect[CommandSender] =
      TargetedEffects.delay(_.sendMessage(stringList.toArray))
  }

}
