package com.github.unchama.itemmigration.controllers.player

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.{PlayerDropItemEvent, PlayerEvent, PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{Cancellable, EventHandler, EventPriority, Listener}

/**
 * プレーヤーのアイテムマイグレーション処理中に、
 * 該当プレーヤーの行動を制御するためのリスナオブジェクトのクラス
 */
class PlayerItemMigrationController(private val migrationState: PlayerItemMigrationStateRepository) extends Listener {
  private def cancelIfLockActive(player: Player, event: Cancellable): Unit = {
    if (migrationState(player).fiber.isComplete.unsafeRunSync()) {
      event.setCancelled(true)
    }
  }

  private def cancelIfLockActive(event: PlayerEvent with Cancellable): Unit =
    cancelIfLockActive(event.getPlayer, event)

  @EventHandler(priority = EventPriority.LOWEST)
  def onInventoryOpen(e: InventoryOpenEvent): Unit = {
    e.getPlayer match {
      case player: Player => cancelIfLockActive(player, e)
      case _ =>
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  def onDropItem(e: PlayerDropItemEvent): Unit = cancelIfLockActive(e)

  @EventHandler(priority = EventPriority.LOWEST)
  def onItemConsume(e: PlayerItemConsumeEvent): Unit = cancelIfLockActive(e)

  @EventHandler(priority = EventPriority.LOWEST)
  def onJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer
    migrationState(player).resumeWith(player).unsafeRunSync()
  }
}
