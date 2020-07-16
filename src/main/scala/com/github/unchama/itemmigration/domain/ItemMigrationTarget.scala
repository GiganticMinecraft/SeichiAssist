package com.github.unchama.itemmigration.domain

/**
 * アイテムマイグレーションを行う対象。
 */
trait ItemMigrationTarget[F[_]] {

  def runMigration(conversion: ItemStackConversion): F[Unit]

}
