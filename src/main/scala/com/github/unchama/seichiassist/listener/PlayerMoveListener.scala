package com.github.unchama.seichiassist.listener

import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.{EventHandler, Listener}

class PlayerMoveListener(implicit effectEnvironment: EffectEnvironment) extends Listener {
  @EventHandler
  def onPlayerMoveToVoid(event: PlayerMoveEvent): Unit = {
    if (event.getTo.getY < 0.0) {
      val player = event.getPlayer
      event.setTo(player.getWorld.getSpawnLocation)
      player.sendMessage(s"${ChatColor.RED}voidに突入していたためスポーン地点にテレポートしました。")
    }
  }
}