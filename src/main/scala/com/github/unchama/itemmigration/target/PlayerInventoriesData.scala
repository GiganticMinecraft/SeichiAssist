package com.github.unchama.itemmigration.target

import cats.effect.IO
import com.github.unchama.itemmigration.ItemMigration.ItemConversion
import com.github.unchama.itemmigration.{ItemMigrationTarget, MigrationHelper}
import org.bukkit.entity.Player

class PlayerInventoriesData(player: Player) extends ItemMigrationTarget[IO] {

  override def runMigration(conversion: ItemConversion): IO[Unit] = IO {
    MigrationHelper.convertEachStackIn(player.getInventory)(conversion)
    MigrationHelper.convertEachStackIn(player.getEnderChest)(conversion)
  }

}
