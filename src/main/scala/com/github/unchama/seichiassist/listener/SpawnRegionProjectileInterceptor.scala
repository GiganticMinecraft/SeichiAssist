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

    val shooter = projectile.getShooter
    if (shooter == null || !shooter.isInstanceOf[Player]) return

    val isInSpawnRegion = getOneRegion(shooter.asInstanceOf[Player].getLocation).filter(rg => rg.getId == "spawn").isPresent
    if (isInSpawnRegion) event.setCancelled(true)
  }
}