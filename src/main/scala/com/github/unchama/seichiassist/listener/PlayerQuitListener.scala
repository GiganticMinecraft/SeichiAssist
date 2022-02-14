package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener extends Listener {
  @EventHandler
  def updateLastQuit(event: PlayerQuitEvent): Unit = {
    SeichiAssist.databaseGateway.playerDataManipulator.updateLastQuit(event.getPlayer.getUniqueId)
  }
}
