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
