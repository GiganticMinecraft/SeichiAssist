package com.github.unchama.itemmigration

import com.github.unchama.itemmigration.ItemMigration.ItemConversion

/**
 * アイテムマイグレーションを行う対象。
 */
trait ItemMigrationTarget[F[_]] {
  def runMigration(conversion: ItemConversion): F[Unit]
}
