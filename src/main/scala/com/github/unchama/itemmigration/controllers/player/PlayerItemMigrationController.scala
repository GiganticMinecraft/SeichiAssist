package com.github.unchama.itemmigration.controllers.player

import cats.effect.IO
import cats.effect.concurrent.TryableDeferred
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import com.github.unchama.playerdatarepository.PreLoginToQuitPlayerDataRepository
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.{PlayerDropItemEvent, PlayerEvent, PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{Cancellable, EventHandler, EventPriority, Listener}

/**
 * プレーヤーのアイテムマイグレーション処理中に、
 * 該当プレーヤーの行動を制御するためのリスナオブジェクトのクラス
 */
class PlayerItemMigrationController(migrationState: PreLoginToQuitPlayerDataRepository[TryableDeferred[IO, Unit]],
                                    migrations: ItemMigrations,
                                    service: ItemMigrationService[IO, PlayerInventoriesData[IO]])
  extends Listener {

  private def cancelIfLockActive(player: Player, event: Cancellable): Unit = {
    if (migrationState(player).tryGet.unsafeRunSync().isEmpty) {
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
  def onBlockPlace(e: BlockPlaceEvent): Unit = cancelIfLockActive(e.getPlayer, e)

  @EventHandler(priority = EventPriority.LOWEST)
  def onDropItem(e: PlayerDropItemEvent): Unit = cancelIfLockActive(e)

  @EventHandler(priority = EventPriority.LOWEST)
  def onItemConsume(e: PlayerItemConsumeEvent): Unit = cancelIfLockActive(e)

  @EventHandler(priority = EventPriority.LOWEST)
  def onJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    for {
      _ <- service.runMigration(migrations)(PlayerInventoriesData(player))
      _ <- migrationState(player).complete(())
    } yield ()
  }.unsafeRunAsyncAndForget()
}
