package com.github.unchama.itemmigration

import org.bukkit.inventory.ItemStack

/**
 * マイグレーションを行う対象。
 */
trait MigrationTarget[F[_]] {
  def runMigration(conversion: ItemStack => ItemStack): F[Unit]
}
