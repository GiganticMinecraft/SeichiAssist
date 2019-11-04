package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

object CommandEffect {

  implicit class StringToCommandEffect(val string: String) {
    def asCommandEffect(): TargetedEffect[Player] =
      targetedeffect.delay { p =>
        // p.performCommandだとBukkitで管理されていないコマンド(例: /hub)などが捕捉されない
        p.chat(s"/$string")
      }
  }

}
