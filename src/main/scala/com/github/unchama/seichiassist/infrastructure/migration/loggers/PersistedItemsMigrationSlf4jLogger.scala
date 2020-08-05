package com.github.unchama.seichiassist.infrastructure.migration.loggers

import cats.effect.Sync
import com.github.unchama.itemmigration.domain.{ItemMigrationLogger, ItemMigrationVersionNumber}
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems
import org.slf4j.Logger

class PersistedItemsMigrationSlf4jLogger[F[_]](logger: Logger)(implicit F: Sync[F])
  extends ItemMigrationLogger[F, SeichiAssistPersistedItems[F]] {

  override def logMigrationVersionsToBeApplied(versions: IndexedSeq[ItemMigrationVersionNumber],
                                               target: SeichiAssistPersistedItems[F]): F[Unit] = {
    val concatenatedVersionString = versions.map(_.versionString).mkString(", ")

    F.delay {
      logger.info("DBに保存されたインベントリにアイテム変換を適用します…")
      logger.info(s"適用するバージョン： $concatenatedVersionString")
    }
  }

}
