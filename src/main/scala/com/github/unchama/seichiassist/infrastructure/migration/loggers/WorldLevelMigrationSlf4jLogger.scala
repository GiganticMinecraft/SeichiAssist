package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.IO
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrationVersionNumber}
import com.github.unchama.itemmigration.targets.WorldLevelData
import org.slf4j.Logger

class WorldLevelMigrationSlf4jLogger(logger: Logger) extends ItemMigrationLogger[IO, WorldLevelData] {

  override def logMigrationVersionsToBeApplied(versions: IndexedSeq[ItemMigrationVersionNumber],
                                               target: WorldLevelData): IO[Unit] = {
    val concatenatedVersionString = versions.map(_.versionString).mkString(", ")

    IO {
      logger.info(s"ワールドデータ内のアイテム変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
