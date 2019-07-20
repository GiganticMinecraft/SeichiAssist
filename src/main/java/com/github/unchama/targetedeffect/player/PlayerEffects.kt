package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player

val closeInventoryEffect = TargetedEffect<Player> { it.closeInventory() }
