package com.github.unchama.targetedeffect.player

import cats.effect.IO
import com.github.unchama.generic
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

object ForcedPotionEffect {
  trait Tag

  def apply(effect: PotionEffect): ForcedPotionEffect = {
    val potionEffect = TargetedEffect.delay[IO, Player] { player =>
      player.addPotionEffect(effect)
    }

    generic.tag.tag.apply[Tag].apply[TargetedEffect[Player]](potionEffect)
  }

  implicit class PotionEffectOps(val potionEffect: PotionEffect) {
    def asTargetedEffect(): ForcedPotionEffect = ForcedPotionEffect(potionEffect)
  }
}
