package com.github.unchama.itemmigration.domain

/**
 * アイテムマイグレーションを行う対象。
 */
trait ItemMigrationTarget[F[_]] {

  /**
   * `conversion` を用いて対象が持ちうるアイテムスタックをすべて変換するような計算。
   */
  def runMigration(conversion: ItemStackConversion): F[Unit]

}
