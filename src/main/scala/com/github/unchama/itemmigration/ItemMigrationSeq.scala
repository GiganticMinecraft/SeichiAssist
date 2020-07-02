package com.github.unchama.itemmigration

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive

case class ItemMigrationSeq(migrations: IndexedSeq[ItemMigration]) {
  private implicit val versionComponentOrdering: Ordering[Int Refined Positive] = Ordering.by(_.value)

  import Ordering.Implicits._

  def sortedMigrations: IndexedSeq[ItemMigration] = migrations.sortBy(_.version)
}
