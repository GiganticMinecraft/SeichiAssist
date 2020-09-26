package com.github.unchama.seichiassist.listener

import org.bukkit.entity.Player
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.meta.PotionMeta
import com.github.unchama.seichiassist.data.HalloweenItemData
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.potion.{PotionEffect, PotionEffectType}

class HalloweenItemListener extends Listener {

  @EventHandler
  def onPlayerConsumeHalloweenPotion(event: PlayerItemConsumeEvent): Unit = {
    val player = event.getPlayer
    val item = event.getItem
    val itemMeta = item.getItemMeta

    if (!item.hasItemMeta || !itemMeta.hasLore || !itemMeta.isInstanceOf[PotionMeta]) return

    val hwPotionMeta = HalloweenItemData.getHalloweenPotion().getItemMeta

    if (itemMeta.getLore == hwPotionMeta.getLore && itemMeta.getDisplayName == hwPotionMeta.getDisplayName) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 12000, 0), true)
    }
  }
}