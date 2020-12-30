package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class ExpBarDesynchronizationListener extends Listener {
  @EventHandler(priority = EventPriority.LOWEST)
  def onPlayerQuitEvent(event: PlayerQuitEvent): Unit = {
    SeichiAssist.instance.expBarSynchronization.desynchronizeFor(event.getPlayer)
  }
}
