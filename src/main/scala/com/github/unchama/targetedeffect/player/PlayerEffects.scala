package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.entity.Player

object PlayerEffects {
  val closeInventoryEffect: TargetedEffect[Player] = TargetedEffect[Player] {
    _.closeInventory()
  }
}
