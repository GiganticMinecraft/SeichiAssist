package com.github.unchama.seichiassist.listener

import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.{EventHandler, Listener}
import com.github.unchama.seichiassist.data.HalloweenItemData.isHalloweenPotion
import org.bukkit.potion.{PotionEffect, PotionEffectType}

class HalloweenItemListener extends Listener {

  @EventHandler
  def onPlayerConsumeHalloweenPotion(event: PlayerItemConsumeEvent): Unit = {
    if (isHalloweenPotion(event.getItem)) {
      // 1.12.2では、Saturationのポーションは効果がないので、PotionEffectとして直接Playerに付与する
      // 10分
      event.getPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 60 * 10, 0), true)
    }
  }
}