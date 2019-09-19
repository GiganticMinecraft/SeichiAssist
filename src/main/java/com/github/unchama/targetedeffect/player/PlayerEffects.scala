package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

object PlayerEffects {
  val closeInventoryEffect = TargetedEffect[Player] { cont => _.closeInventory() }
}
