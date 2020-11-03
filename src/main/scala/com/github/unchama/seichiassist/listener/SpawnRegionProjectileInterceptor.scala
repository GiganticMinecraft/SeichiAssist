package com.github.unchama.seichiassist.listener

import com.github.unchama.util.external.WorldGuardWrapper.getOneRegion
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.{EventHandler, Listener}

object SpawnRegionProjectileInterceptor extends Listener {
  @EventHandler
  def onProjectileLaunch(event: ProjectileLaunchEvent): Unit = {
    val projectile = event.getEntity
    if (projectile == null) return

    projectile.getShooter match {
      case player: Player =>
        val isInSpawnRegion = getOneRegion(player.getLocation).filter(_.getId == "spawn").isPresent
        if (isInSpawnRegion) event.setCancelled(true)
      case _ =>
    }
  }
}