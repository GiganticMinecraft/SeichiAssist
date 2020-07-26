package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.Sync
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrations}
import com.github.unchama.itemmigration.targets.WorldLevelData
import org.slf4j.Logger

class WorldLevelMigrationSlf4jLogger[F[_] : Sync](logger: Logger) extends ItemMigrationLogger[F, WorldLevelData] {

  override def logMigrationsToBeApplied(versions: ItemMigrations, target: WorldLevelData): F[Unit] = {
    val versionsToBeApplied = versions.migrations.map(_.version.mkString("."))
    val concatenatedVersionString = versionsToBeApplied.mkString(", ")

    Sync[F].delay {
      logger.info(s"ワールドデータ内のアイテム変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
