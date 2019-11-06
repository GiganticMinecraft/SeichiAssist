package com.github.unchama.targetedeffect.player

import com.github.unchama
import com.github.unchama.generic
import com.github.unchama.generic.tag.tag.@@
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

object ForcedPotionEffect {
  trait Tag
  type ForcedPotionEffect = TargetedEffect[Player] @@ Tag

  def apply(effect: PotionEffect): ForcedPotionEffect = {
    val potionEffect = unchama.targetedeffect.delay { player: Player => player.addPotionEffect(effect) }

    generic.tag.tag
      .apply[Tag]
      .apply[TargetedEffect[Player]](potionEffect)
  }

  implicit class PotionEffectOps(val potionEffect: PotionEffect) {
    def asTargetedEffect(): ForcedPotionEffect = ForcedPotionEffect(potionEffect)
  }
}
