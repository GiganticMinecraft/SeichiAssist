package com.github.unchama.itemmigration.domain

case class ItemMigrations(migrations: IndexedSeq[ItemMigration]) {

  private implicit val versionComponentOrdering: Ordering[ItemMigrationVersionComponent] = {
    Ordering.by(_.value)
  }

  def sorted: ItemMigrations = {
    import Ordering.Implicits._

    ItemMigrations(migrations.sortBy(_.version))
  }

  def yetToBeApplied(appliedVersions: Set[ItemMigrationVersionNumber]): ItemMigrations =
    ItemMigrations {
      migrations.filter(m => !appliedVersions.contains(m.version))
    }

  def toSingleConversion: ItemStackConversion = {
    migrations.map(_.conversion).reduce((c1, c2) => c1.andThen(c2))
  }

  def versions: List[ItemMigrationVersionNumber] = {
    migrations.map(_.version).toList
  }

}
