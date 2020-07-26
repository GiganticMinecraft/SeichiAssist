package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.Sync
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrations}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import org.slf4j.Logger

class PlayerItemsMigrationSlf4jLogger[F[_] : Sync](logger: Logger) extends ItemMigrationLogger[F, PlayerInventoriesData] {

  override def logMigrationsToBeApplied(versions: ItemMigrations, target: PlayerInventoriesData): F[Unit] = {
    val versionsToBeApplied = versions.migrations.map(_.version.mkString("."))
    val concatenatedVersionString = versionsToBeApplied.mkString(", ")

    Sync[F].delay {
      logger.info(s"${target.player.getName} (UUID: ${target.player.getUniqueId})のインベントリ内データ変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
