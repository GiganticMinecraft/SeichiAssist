package com.github.unchama.seichiassist.infrastructure.migration.targets

import cats.effect.IO
import com.github.unchama.itemmigration.domain.{ItemMigrationTarget, ItemStackConversion}

object SeichiAssistPersistedItems extends ItemMigrationTarget[IO] {

  override def runMigration(conversion: ItemStackConversion): IO[Unit] = {
    ???
  }

}
