package com.github.unchama.seichiassist.infrastructure.migration.persistenceproviders

import cats.effect.IO
import com.github.unchama.itemmigration.service.ItemMigrationPersistence
import com.github.unchama.seichiassist.itemmigration.SeichiAssistWorldLevelData

object WorldLevelDataMigrationPersistence {

  def provider(): ItemMigrationPersistence.Provider[IO, SeichiAssistWorldLevelData.type] = ???

}
