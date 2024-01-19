package com.github.unchama.seichiassist.subsystems.canceldamagebyfallingblocks.bukkit.listeners

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.{EventHandler, Listener}

object PlayerDamageByBlockEvents extends Listener {

  @EventHandler
  def onDamage(e: EntityDamageByBlockEvent): Unit = {
    e.getEntity match {
      case _: Player =>
        println(s"block: ${e.getDamager.getType}")
        e.setDamage(0.0)
      case _ =>
    }
  }

}
