package com.github.unchama.itemmigration

import cats.effect.Bracket
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationSeq, ItemMigrationTarget}
import com.github.unchama.itemmigration.service.ItemMigrationPersistence

case class ItemMigrationConfiguration[F[_], T <: ItemMigrationTarget[F]](migrationSeq: ItemMigrationSeq,
                                                                         migrationTarget: T,
                                                                         persistenceProvider: ItemMigrationPersistence.Provider[F, T])
                                                                        (implicit F: Bracket[F, Throwable]) {

  def run: F[Unit] = {
    val sortedMigrationSeq = migrationSeq.sorted

    import cats.implicits._
    persistenceProvider.use { persistence =>
      for {
        requiredMigrations <- persistence.filterRequiredMigrations(migrationTarget)(sortedMigrationSeq)
        unifiedConversion = requiredMigrations.toSingleConversion
        _ <- migrationTarget.runMigration(unifiedConversion)
        _ <- persistence.writeCompletedMigrations(migrationTarget)(requiredMigrations)
      } yield ()
    }
  }

}
