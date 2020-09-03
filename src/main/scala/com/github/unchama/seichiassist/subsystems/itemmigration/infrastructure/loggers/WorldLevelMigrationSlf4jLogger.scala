package com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers

import cats.effect.Sync
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrationVersionNumber}
import com.github.unchama.itemmigration.targets.WorldLevelData
import org.slf4j.Logger

class WorldLevelMigrationSlf4jLogger[F[_]](logger: Logger)(implicit val F: Sync[F])
  extends ItemMigrationLogger[F, WorldLevelData[F]] {

  override def logMigrationVersionsToBeApplied(versions: IndexedSeq[ItemMigrationVersionNumber],
                                               target: WorldLevelData[F]): F[Unit] = {
    val concatenatedVersionString = versions.map(_.versionString).mkString(", ")

    F.delay {
      logger.info(s"ワールドデータ内のアイテム変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
