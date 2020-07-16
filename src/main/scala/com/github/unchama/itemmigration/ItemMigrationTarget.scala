package com.github.unchama.itemmigration

import com.github.unchama.itemmigration.ItemMigration.ItemStackConversion

/**
 * アイテムマイグレーションを行う対象。
 */
trait ItemMigrationTarget[F[_]] {

  def runMigration(conversion: ItemStackConversion): F[Unit]

}
