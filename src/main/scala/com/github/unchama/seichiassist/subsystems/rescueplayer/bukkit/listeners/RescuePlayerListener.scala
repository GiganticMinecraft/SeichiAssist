package com.github.unchama.seichiassist.subsystems.rescueplayer.bukkit.listeners

import org.bukkit.ChatColor._
import org.bukkit.event.player.{PlayerJoinEvent, PlayerMoveEvent}
import org.bukkit.event.{EventHandler, Listener}

class RescuePlayerListener extends Listener {
  @EventHandler
  def onPlayerFallenToVoid(event: PlayerMoveEvent): Unit = {
    if (event.getTo.getBlockY < 0.0) {
      val player = event.getPlayer
      val worldSpawnLocation = player.getWorld.getSpawnLocation

      player.teleport(worldSpawnLocation)
      player.sendMessage(s"${RED}voidに落下していたため、ワールドのスポーン地点にテレポートしました。")
    }
  }

  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    if (event.getPlayer.isDead) {
      val player = event.getPlayer
      val worldSpawnLocation = player.getWorld.getSpawnLocation

      player.teleport(worldSpawnLocation)
    }
  }
}
