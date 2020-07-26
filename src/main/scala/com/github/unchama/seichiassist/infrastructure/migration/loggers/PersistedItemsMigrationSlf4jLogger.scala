package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.Sync
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrations}
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems
import org.slf4j.Logger

class PersistedItemsMigrationSlf4jLogger[F[_] : Sync](logger: Logger) extends ItemMigrationLogger[F, SeichiAssistPersistedItems.type] {

  override def logMigrationsToBeApplied(versions: ItemMigrations, target: SeichiAssistPersistedItems.type): F[Unit] = {
    val versionsToBeApplied = versions.migrations.map(_.version.mkString("."))
    val concatenatedVersionString = versionsToBeApplied.mkString(", ")

    Sync[F].delay {
      logger.info("DBに保存されたインベントリにアイテム変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
