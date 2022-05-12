package com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners

import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}

class GachaController extends Listener {

  @EventHandler
  def onPlayerRightClickGachaEvent(event: PlayerInteractEvent): Unit = {}

}
