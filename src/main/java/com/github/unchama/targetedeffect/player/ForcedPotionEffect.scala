package com.github.unchama.targetedeffect.player

import com.github.unchama.targetedeffect.TargetedEffect
import kotlin.coroutines.Continuation
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

case class ForcedPotionEffect(val effect: PotionEffect) extends TargetedEffect[Player] {
  override def runFor(minecraftObject: Player, continuation: Continuation[Unit]) {
    minecraftObject.addPotionEffect(effect)
  }
}

object ForcedPotionEffect {
  implicit class PotionEffectOps(val potionEffect: PotionEffect) {
    def asTargetedEffect(): ForcedPotionEffect = ForcedPotionEffect(potionEffect)
  }
}
