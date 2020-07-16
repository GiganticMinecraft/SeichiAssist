package com.github.unchama.itemmigration.domain

import com.github.unchama.itemmigration.domain.ItemMigration.ItemStackConversion

/**
 * アイテムマイグレーションを行う対象。
 */
trait ItemMigrationTarget[F[_]] {

  def runMigration(conversion: ItemStackConversion): F[Unit]

}
