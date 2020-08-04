package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.IO
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrationVersionNumber}
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems
import org.slf4j.Logger

class PersistedItemsMigrationSlf4jLogger(logger: Logger) extends ItemMigrationLogger[IO, SeichiAssistPersistedItems] {

  override def logMigrationVersionsToBeApplied(versions: IndexedSeq[ItemMigrationVersionNumber],
                                               target: SeichiAssistPersistedItems): IO[Unit] = {
    val concatenatedVersionString = versions.map(_.versionString).mkString(", ")

    IO {
      logger.info("DBに保存されたインベントリにアイテム変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
