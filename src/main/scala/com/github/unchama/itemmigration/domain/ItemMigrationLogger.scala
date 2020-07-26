package com.github.unchama.itemmigration.domain

trait ItemMigrationLogger[F[_], -T <: ItemMigrationTarget[F]] {

  /**
   * `target` に対して、 `versions` をバージョンに持つマイグレーションを適用する旨をログ等で管理者に通知する。
   */
  def logMigrationVersionsToBeApplied(versions: IndexedSeq[ItemMigrationVersionNumber], target: T): F[Unit]

}
