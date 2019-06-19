package com.github.unchama.seichiassist.menus

import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.computedEffect
import org.bukkit.entity.Player

object MineStackUI {
  val open: TargetedEffect<Player> = computedEffect { player ->
    EmptyEffect
  }
}