package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.{TargetedEffect, TargetedEffects}
import org.bukkit.entity.Player

object PlayerEffects {
  val closeInventoryEffect: TargetedEffect[Player] = TargetedEffects.delay(_.closeInventory())
}
