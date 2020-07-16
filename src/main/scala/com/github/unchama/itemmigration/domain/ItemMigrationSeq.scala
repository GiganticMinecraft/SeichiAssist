package com.github.unchama.itemmigration.domain

case class ItemMigrationSeq(migrations: IndexedSeq[ItemMigration]) {
  private implicit val versionComponentOrdering: Ordering[ItemMigrationVersionComponent] = Ordering.by(_.value)

  import Ordering.Implicits._

  def sorted: ItemMigrationSeq =
    ItemMigrationSeq {
      migrations.sortBy(_.version)
    }

  def yetToBeApplied(appliedVersions: Set[ItemMigrationVersionNumber]): ItemMigrationSeq =
    ItemMigrationSeq {
      migrations.filter(m => !appliedVersions.contains(m.version))
    }

  def toSingleConversion: ItemStackConversion = {
    migrations.map(_.conversion).reduce((c1, c2) => c1.andThen(c2))
  }

}
