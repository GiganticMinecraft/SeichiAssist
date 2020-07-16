package com.github.unchama.itemmigration.domain

import cats.effect.{Bracket, Resource}

trait VersionedItemMigrationExecutor[F[_], -T <: ItemMigrationTarget[F]] {

  type PersistenceLock[TInstance <: Singleton with T]

  implicit val F: Bracket[F, Throwable]

  def lockVersionPersistence(target: T): Resource[F, PersistenceLock[target.type]]

  def getVersionsAppliedTo(target: T): PersistenceLock[target.type] => F[Set[ItemMigrationVersionNumber]]

  def persistVersionsAppliedTo(target: T, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => F[Unit]

  final def runMigration(migrations: ItemMigrations)(target: T): F[Unit] = {
    import cats.implicits._

    lockVersionPersistence(target).use { implicit lock =>
      for {
        appliedVersions <- getVersionsAppliedTo(target)(lock)
        migrationsToApply = migrations.yetToBeApplied(appliedVersions)
        _ <- target.runMigration(migrationsToApply.toSingleConversion)
        _ <- persistVersionsAppliedTo(target, migrationsToApply.versions)(lock)
      } yield ()
    }
  }

}
