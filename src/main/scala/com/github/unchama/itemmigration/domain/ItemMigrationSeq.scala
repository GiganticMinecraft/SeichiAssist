package com.github.unchama.itemmigration.domain

import com.github.unchama.itemmigration.domain.ItemMigration.VersionComponent

case class ItemMigrationSeq(migrations: IndexedSeq[ItemMigration]) {
  private implicit val versionComponentOrdering: Ordering[VersionComponent] = Ordering.by(_.value)

  import Ordering.Implicits._

  def sortedMigrations: IndexedSeq[ItemMigration] = migrations.sortBy(_.version)
}
