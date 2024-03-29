package com.github.unchama.itemmigration.bukkit.controllers.player

import cats.effect.{ConcurrentEffect, SyncEffect, SyncIO}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.itemmigration.bukkit.targets.PlayerInventoriesData
import com.github.unchama.itemmigration.domain.{ItemMigrations, PlayerMigrationState}
import com.github.unchama.itemmigration.service.ItemMigrationService
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.{
  PlayerDropItemEvent,
  PlayerEvent,
  PlayerItemConsumeEvent,
  PlayerJoinEvent
}
import org.bukkit.event.{Cancellable, EventHandler, EventPriority, Listener}

/**
 * プレーヤーのアイテムマイグレーション処理中に、 該当プレーヤーの行動を制御するためのリスナオブジェクトのクラス
 */
class PlayerItemMigrationController[F[_]: ConcurrentEffect, G[_]: SyncEffect](
  migrationState: KeyedDataRepository[Player, PlayerMigrationState[G]],
  migrations: ItemMigrations,
  service: ItemMigrationService[F, PlayerInventoriesData[F]]
) extends Listener {

  import cats.effect.implicits._

  private def cancelIfLockActive(player: Player, event: Cancellable): Unit = {
    if (!migrationState(player).hasMigrated.runSync[SyncIO].unsafeRunSync()) {
      event.setCancelled(true)
    }
  }

  private def cancelIfLockActive(event: PlayerEvent with Cancellable): Unit =
    cancelIfLockActive(event.getPlayer, event)

  @EventHandler(priority = EventPriority.LOWEST)
  def onInventoryOpen(e: InventoryOpenEvent): Unit = {
    e.getPlayer match {
      case player: Player => cancelIfLockActive(player, e)
      case _              =>
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

    import cats.implicits._

    for {
      _ <- service.runMigration(migrations)(PlayerInventoriesData(player))
      _ <- ContextCoercion[G, F, Unit](migrationState(player).setMigrated)
    } yield ()
  }.toIO.unsafeRunAsyncAndForget()
}
