package com.github.unchama.itemmigration.domain

trait ItemMigrationLogger[F[_], -T <: ItemMigrationTarget[F]] {

  /**
   * `target` に対して `versions` を適用する旨をログ等で管理者に通知する。
   */
  def logMigrationsToBeApplied(versions: ItemMigrations, target: T): F[Unit]

}
