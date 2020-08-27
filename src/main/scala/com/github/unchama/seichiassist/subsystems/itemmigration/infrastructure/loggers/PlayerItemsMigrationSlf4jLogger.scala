package com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers

import cats.effect.Sync
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrationVersionNumber}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import org.slf4j.Logger

class PlayerItemsMigrationSlf4jLogger[F[_] : Sync](logger: Logger) extends ItemMigrationLogger[F, PlayerInventoriesData[F]] {

  override def logMigrationVersionsToBeApplied(versions: IndexedSeq[ItemMigrationVersionNumber],
                                               target: PlayerInventoriesData[F]): F[Unit] = {
    val concatenatedVersionString = versions.map(_.versionString).mkString(", ")

    Sync[F].delay {
      logger.info(s"${target.player.getName} (UUID: ${target.player.getUniqueId})のインベントリ内データ変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
