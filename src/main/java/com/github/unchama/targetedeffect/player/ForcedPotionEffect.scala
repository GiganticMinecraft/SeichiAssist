package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

case class ForcedPotionEffect(val effect: PotionEffect) extends TargetedEffect[Player] {
  override @SuspendingMethod def runFor(minecraftObject: Player) {
    minecraftObject.addPotionEffect(effect)
  }
}

object ForcedPotionEffect {
  implicit class PotionEffectOps(val potionEffect: PotionEffect) {
    def asTargetedEffect(): ForcedPotionEffect = ForcedPotionEffect(potionEffect)
  }
}
