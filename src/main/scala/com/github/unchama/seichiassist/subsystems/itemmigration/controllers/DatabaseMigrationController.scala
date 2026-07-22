package com.github.unchama.seichiassist.subsystems.itemmigration.controllers

import cats.effect.Sync
import com.github.unchama.generic.UnsafeSyncRunner
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.PersistedItemsMigrationSlf4jLogger
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.PersistedItemsMigrationVersionRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.targets.SeichiAssistPersistedItems
import org.slf4j.Logger
import scalikejdbc.DB

case class DatabaseMigrationController[F[_]: Sync: UnsafeSyncRunner](
  migrations: ItemMigrations
)(implicit logger: Logger) {

  lazy val runDatabaseMigration: F[Unit] = Sync[F].delay {
    DB.autoCommit { implicit session =>
      // DB内アイテムのマイグレーション
      val migration = ItemMigrationService
        .inContextOf[F](
          new PersistedItemsMigrationVersionRepository(),
          new PersistedItemsMigrationSlf4jLogger(logger)
        )
        .runMigration(migrations)(new SeichiAssistPersistedItems())
      UnsafeSyncRunner[F].unsafeRunSync(migration)
    }
  }

}
