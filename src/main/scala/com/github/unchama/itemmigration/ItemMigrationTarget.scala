package com.github.unchama.itemmigration

import org.bukkit.inventory.ItemStack

/**
 * アイテムマイグレーションを行う対象。
 */
trait ItemMigrationTarget[F[_]] {
  def runMigration(conversion: ItemStack => ItemStack): F[Unit]
}
