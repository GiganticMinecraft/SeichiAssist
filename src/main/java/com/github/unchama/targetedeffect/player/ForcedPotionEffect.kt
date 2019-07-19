package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

data class ForcedPotionEffect(val effect: PotionEffect): TargetedEffect<Player> {
  override suspend fun runFor(minecraftObject: Player) {
    minecraftObject.addPotionEffect(effect)
  }
}

fun PotionEffect.asTargetedEffect(): ForcedPotionEffect = ForcedPotionEffect(this)
