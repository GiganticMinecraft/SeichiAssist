package com.github.unchama.seichiassist.infrastructure.migration

import cats.effect.IO
import com.github.unchama.itemmigration.domain.ItemMigration.ItemStackConversion
import com.github.unchama.itemmigration.domain.ItemMigrationTarget

object SeichiAssistPersistedItems extends ItemMigrationTarget[IO] {

  override def runMigration(conversion: ItemStackConversion): IO[Unit] = {
    ???
  }

}
