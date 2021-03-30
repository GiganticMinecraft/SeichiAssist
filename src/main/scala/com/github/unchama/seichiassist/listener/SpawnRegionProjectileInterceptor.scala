package com.github.unchama.seichiassist.listener

import com.github.unchama.util.external.WorldGuardWrapper.getRegions
import org.bukkit.Material._
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}

import scala.jdk.CollectionConverters._

object SpawnRegionProjectileInterceptor extends Listener {
  val spawnRegions = Set(
    // 基本の保護名
    "spawn",
    // メインワールドにおいて、スポーン地点を保護している保護名
    "spawn-center",
    // 公共施設サーバーのスポーン地点名
    "world-spawn"
  )
  val projectiles = Set(
    BOW, EGG, LINGERING_POTION, SPLASH_POTION, ENDER_PEARL, EYE_OF_ENDER, SNOW_BALL, EXP_BOTTLE
  )

  @EventHandler
  def beforeProjectileLaunch(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer
    val action = event.getAction

    // Projectileを持った状態で右クリックし、playerがいる保護がspawn保護の中であった場合はイベントをキャンセルする
    if (event.hasItem
      && projectiles.contains(event.getItem.getType)
      && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
      && getRegions(player.getLocation).asScala.map(_.getId).exists(spawnRegions.contains)) {
      event.setCancelled(true)
    }
  }
}
