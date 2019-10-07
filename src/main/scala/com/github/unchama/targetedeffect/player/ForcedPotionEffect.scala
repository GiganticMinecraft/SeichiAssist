package com.github.unchama.targetedeffect.player

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

case class ForcedPotionEffect(effect: PotionEffect) extends TargetedEffect[Player] {
  override def apply(v1: Player): IO[Unit] = IO {
    v1.addPotionEffect(effect)
  }
}

object ForcedPotionEffect {

  implicit class PotionEffectOps(val potionEffect: PotionEffect) {
    def asTargetedEffect(): ForcedPotionEffect = ForcedPotionEffect(potionEffect)
  }

}
