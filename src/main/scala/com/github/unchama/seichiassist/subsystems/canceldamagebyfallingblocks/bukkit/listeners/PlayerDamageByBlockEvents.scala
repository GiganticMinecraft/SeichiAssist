package com.github.unchama.seichiassist.subsystems.canceldamagebyfallingblocks.bukkit.listeners

import org.bukkit.Material
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.entity.Player

object PlayerDamageByBlockEvents extends Listener {

  @EventHandler
  def onDamage(e: EntityDamageByBlockEvent): Unit = {
    e.getEntity match {
      case player: Player if e.getDamager.getType == Material.POINTED_DRIPSTONE =>
        e.setCancelled(true)
      case _ =>
    }
  }

}
