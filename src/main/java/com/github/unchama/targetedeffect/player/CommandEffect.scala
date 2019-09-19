package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

object CommandEffect {
  implicit class StringToCommandEffect(val string: String) {
    def asCommandEffect(): TargetedEffect[Player] = TargetedEffect[Player] { _ => _.performCommand(string) }
  }
}
