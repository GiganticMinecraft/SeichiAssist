package com.github.unchama.seichiassist.subsystems.canceldamagebyfallingblocks.bukkit.listeners

import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.{EventHandler, Listener}
import com.github.unchama.seichiassist.ManagedWorld._

object PlayerDamageByBlockEvents extends Listener {

  @EventHandler
  def onDamage(e: EntityDamageEvent): Unit = {
    e.getEntity match {
      // 整地ワールドでは落下ダメージを無効化する
      case player: Player
          if e.getCause == DamageCause.FALLING_BLOCK && player.getWorld.isSeichi =>
        e.setDamage(0.0)
      case _ =>
    }
  }

}
