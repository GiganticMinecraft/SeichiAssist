package com.github.unchama.itemmigration

import cats.effect.Bracket

case class ItemMigrationConfiguration[F[_]](migrationSeq: ItemMigrationSeq,
                                            migrationTarget: ItemMigrationTarget[F],
                                            persistenceProvider: ItemMigrationPersistenceProvider[F])
                                           (implicit F: Bracket[F, Throwable]) {

  def run: F[Unit] = {
    val sortedMigrationSeq = migrationSeq.sortedMigrations

    import cats.implicits._
    persistenceProvider.withPersistence.use { persistence =>
      for {
        requiredMigrations <- persistence.filterRequiredMigrations(sortedMigrationSeq)
        unifiedConversion = ItemMigration.toSingleFunction(requiredMigrations)
        _ <- migrationTarget.runMigration(unifiedConversion)
      } yield ()
    }
  }

}
