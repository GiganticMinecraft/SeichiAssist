package com.github.unchama.seichiassist.listener

import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.{EventHandler, Listener}

class PlayerMoveListener(implicit effectEnvironment: EffectEnvironment) extends Listener {
  @EventHandler
  def onPlayerFallenToVoid(event: PlayerMoveEvent): Unit = {
    if (event.getTo.getY < 0.0) {
      val player = event.getPlayer
      player.teleport(player.getWorld.getSpawnLocation)
      player.sendMessage(s"${ChatColor.RED}voidに落下していたため、ワールドのスポーン地点にテレポートしました。")
    }
  }
}