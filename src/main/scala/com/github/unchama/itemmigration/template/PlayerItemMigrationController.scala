package com.github.unchama.itemmigration.template

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.{PlayerDropItemEvent, PlayerEvent, PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{Cancellable, EventHandler, EventPriority, Listener}

class PlayerItemMigrationController(progress: PlayerItemMigrationProgress) extends Listener {
  private def cancelIfLockActive(player: Player, event: Cancellable): Unit = {
    if (progress(player).isComplete.unsafeRunSync()) {
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
    progress(player).invokeWith(player).unsafeRunSync()
  }
}
