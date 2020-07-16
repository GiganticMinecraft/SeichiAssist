package com.github.unchama.itemmigration.target.player

import cats.effect.IO
import com.github.unchama.itemmigration.domain.ItemMigration.ItemStackConversion
import com.github.unchama.itemmigration.domain.ItemMigrationTarget
import com.github.unchama.itemmigration.util.MigrationHelper
import org.bukkit.entity.Player

/**
 * アイテムマイグレーションを行う対象としてのプレーヤーインベントリを表すオブジェクト
 */
class PlayerInventoriesData(player: Player) extends ItemMigrationTarget[IO] {

  override def runMigration(conversion: ItemStackConversion): IO[Unit] = IO {
    MigrationHelper.convertEachStackIn(player.getInventory)(conversion)
    MigrationHelper.convertEachStackIn(player.getEnderChest)(conversion)
  }

}
