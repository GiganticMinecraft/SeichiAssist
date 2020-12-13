package com.github.unchama.itemmigration.service

import cats.effect.Bracket
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrationTarget, ItemMigrationVersionRepository, ItemMigrations}

case class ItemMigrationService[F[_], -T <: ItemMigrationTarget[F]](persistence: ItemMigrationVersionRepository[F, T],
                                                                    logger: ItemMigrationLogger[F, T])
                                                                   (implicit F: Bracket[F, Throwable]) {

  def runMigration(migrations: ItemMigrations)(target: T): F[Unit] = {
    import cats.implicits._

    persistence.lockVersionPersistence(target).use { implicit lock =>
      for {
        appliedVersions <- persistence.getVersionsAppliedTo(target)(lock)
        migrationsToApply = migrations.yetToBeApplied(appliedVersions)
        _ <- logger.logMigrationVersionsToBeApplied(migrationsToApply.migrations.map(_.version), target)
        _ <-
          if (migrationsToApply.isEmpty) F.unit
          else target.runMigration(migrationsToApply.toSingleConversion)
        _ <- persistence.persistVersionsAppliedTo(target, migrationsToApply.versions)(lock)
      } yield ()
    }
  }

}

object ItemMigrationService {

  /**
   * Uses the [[https://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  //noinspection ScalaUnusedSymbol
  final private[service] class ItemMigrationServicePartiallyApplied[F[_]](private val dummy: Boolean = true) extends AnyVal {
    def apply[T <: ItemMigrationTarget[F]](persistence: ItemMigrationVersionRepository[F, T],
                                           logger: ItemMigrationLogger[F, T])
                                          (implicit F: Bracket[F, Throwable]): ItemMigrationService[F, T] = {
      ItemMigrationService[F, T](persistence, logger)
    }
  }

  def inContextOf[F[_]]: ItemMigrationServicePartiallyApplied[F] = new ItemMigrationServicePartiallyApplied[F]
}