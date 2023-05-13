package com.github.unchama.seichiassist.listener

import com.github.unchama.util.external.WorldGuardWrapper.getRegions
import org.bukkit.Material._
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}

object SpawnRegionProjectileInterceptor extends Listener {
  private val spawnRegionNames = Set(
    // 基本の保護名
    "spawn",
    // メインワールドにおいて、スポーン地点を保護している保護名
    "spawn-center",
    // 公共施設サーバーのスポーン地点名
    "world-spawn"
  )
  private val projectiles = Set(
    BOW,
    EGG,
    ENDER_PEARL,
    EXPERIENCE_BOTTLE,
    ENDER_EYE,
    LINGERING_POTION,
    SNOWBALL,
    SPLASH_POTION
  )

  @EventHandler
  def beforeProjectileLaunch(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val action = event.getAction
    val hasProjectile = event.hasItem && projectiles.contains(event.getItem.getType)
    val isRightClickEvent =
      action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK
    val isInSpawnRegion =
      getRegions(player.getLocation).map(_.getId).exists(spawnRegionNames.contains)

    // Projectileを持った状態で右クリックし、playerがいる保護がspawn保護の中であった場合はイベントをキャンセルする
    if (hasProjectile && isRightClickEvent && isInSpawnRegion) {
      event.setCancelled(true)
    }
  }
}
