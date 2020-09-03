package com.github.unchama.seichiassist.subsystems.itemmigration.controllers

import cats.effect.IO
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.itemmigration.service
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.WorldLevelMigrationSlf4jLogger
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.WorldLevelItemsMigrationVersionRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.targets.SeichiAssistWorldLevelData
import org.slf4j.Logger

case class WorldMigrationController(migrations: ItemMigrations)
                                   (implicit effectEnvironment: EffectEnvironment, logger: Logger) {

  lazy val runWorldMigration: IO[Unit] = {
    // ワールド内アイテムのマイグレーション
    // TODO IOを剥がす
    service.ItemMigrationService.inContextOf[IO](
      new WorldLevelItemsMigrationVersionRepository(SeichiAssist.seichiAssistConfig.getServerId),
      new WorldLevelMigrationSlf4jLogger(logger)
    )
      .runMigration(migrations) {
        import PluginExecutionContexts.asyncShift
        new SeichiAssistWorldLevelData()
      }
  }

}
