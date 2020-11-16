package com.github.unchama.seichiassist.listener

import com.github.unchama.util.external.WorldGuardWrapper.{getOneRegion, getRegions}
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.{EventHandler, Listener}

object SpawnRegionProjectileInterceptor extends Listener {
  @EventHandler
  def onProjectileLaunch(event: ProjectileLaunchEvent): Unit = {
    val projectile = event.getEntity
    if (projectile == null) return

    val spawnRegions = Set(
      // 基本の保護名
      "spawn",
      // メインワールドにおいて、スポーン地点を保護している保護名
      "spawn-center",
      // 公共施設サーバーのスポーン地点名
      "world-spawn"
    )

    projectile.getShooter match {
      case player: Player =>
        getRegions(player.getLocation).forEach(rg => if (spawnRegions.contains(rg.getId)) event.setCancelled(true))
      case _ =>
    }
  }
}