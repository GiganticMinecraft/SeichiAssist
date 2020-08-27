package com.github.unchama.seichiassist.subsystems.itemmigration.controllers

import cats.effect.{Sync, SyncEffect}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.PersistedItemsMigrationSlf4jLogger
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.PersistedItemsMigrationVersionRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.targets.SeichiAssistPersistedItems
import org.slf4j.Logger
import scalikejdbc.DB

case class DatabaseMigrationController[F[_] : SyncEffect](migrations: ItemMigrations)
                                                         (implicit effectEnvironment: EffectEnvironment, logger: Logger) {

  lazy val runDatabaseMigration: F[Unit] = Sync[F].delay {
    DB.autoCommit { implicit session =>
      // DB内アイテムのマイグレーション
      ItemMigrationService.inContextOf[F](
        new PersistedItemsMigrationVersionRepository(),
        new PersistedItemsMigrationSlf4jLogger(logger)
      )
        .runMigration(migrations)(new SeichiAssistPersistedItems())
    }
  }

}
