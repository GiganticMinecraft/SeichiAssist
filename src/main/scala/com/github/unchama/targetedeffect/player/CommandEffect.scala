package com.github.unchama.targetedeffect.player

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.entity.Player

object CommandEffect {
  implicit class StringToCommandEffect(val string: String) {
    def asCommandEffect(): TargetedEffect[Player] =
      p => IO {
        // p.performCommandだとBukkitで管理されていないコマンド(例: /hub)などが捕捉されない
        p.chat(s"/$string")
      }
  }
}
