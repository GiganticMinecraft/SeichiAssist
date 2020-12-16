package com.github.unchama.itemmigration.bukkit.targets

import cats.effect.Sync
import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigrationTarget, ItemStackConversion}
import org.bukkit.entity.Player

/**
 * アイテムマイグレーションを行う対象としてのプレーヤーインベントリを表すオブジェクト
 */
case class PlayerInventoriesData[F[_] : Sync](player: Player) extends ItemMigrationTarget[F] {

  override def runMigration(conversion: ItemStackConversion): F[Unit] = Sync[F].delay {
    MigrationHelper.convertEachStackIn(player.getInventory)(conversion)
    MigrationHelper.convertEachStackIn(player.getEnderChest)(conversion)
  }

}
