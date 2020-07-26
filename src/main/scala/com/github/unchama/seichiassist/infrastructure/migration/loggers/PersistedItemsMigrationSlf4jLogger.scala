package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.IO
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrations}
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems
import org.slf4j.Logger

class PersistedItemsMigrationSlf4jLogger(logger: Logger) extends ItemMigrationLogger[IO, SeichiAssistPersistedItems.type] {

  override def logMigrationsToBeApplied(versions: ItemMigrations, target: SeichiAssistPersistedItems.type): IO[Unit] = {
    val versionsToBeApplied = versions.migrations.map(_.version.mkString("."))
    val concatenatedVersionString = versionsToBeApplied.mkString(", ")

    IO {
      logger.info("DBに保存されたインベントリにアイテム変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
