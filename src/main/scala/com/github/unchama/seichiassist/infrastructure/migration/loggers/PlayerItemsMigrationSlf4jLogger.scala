package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.IO
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrationVersionNumber}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import org.slf4j.Logger

class PlayerItemsMigrationSlf4jLogger(logger: Logger) extends ItemMigrationLogger[IO, PlayerInventoriesData] {

  override def logMigrationVersionsToBeApplied(versions: IndexedSeq[ItemMigrationVersionNumber],
                                               target: PlayerInventoriesData): IO[Unit] = {
    val concatenatedVersionString = versions.map(_.versionString).mkString(", ")

    IO {
      logger.info(s"${target.player.getName} (UUID: ${target.player.getUniqueId})のインベントリ内データ変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
