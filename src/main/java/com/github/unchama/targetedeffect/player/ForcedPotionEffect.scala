package com.github.unchama.targetedeffect.player

import org.bukkit.potion.PotionEffect

case class ForcedPotionEffect(val effect: PotionEffect): TargetedEffect<Player> {
  override suspend def runFor(minecraftObject: Player) {
    minecraftObject.addPotionEffect(effect)
  }
}

def PotionEffect.asTargetedEffect(): ForcedPotionEffect = ForcedPotionEffect(this)
