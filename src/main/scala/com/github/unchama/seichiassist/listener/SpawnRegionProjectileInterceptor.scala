package com.github.unchama.seichiassist.listener

import com.github.unchama.util.external.WorldGuardWrapper.{getOneRegion, getRegions}
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.{EventHandler, Listener}

import scala.jdk.CollectionConverters._

object SpawnRegionProjectileInterceptor extends Listener {
  @EventHandler
  def onProjectileLaunch(event: ProjectileLaunchEvent): Unit = {
    val projectile = event.getEntity
    if (projectile == null) return

    projectile.getShooter match {
      case player: Player =>
        val isInSpawnRegion = getRegions(player.getLocation).asScala.exists(_.getId == "spawn")
        if (isInSpawnRegion) event.setCancelled(true)
      case _ =>
    }
  }
}