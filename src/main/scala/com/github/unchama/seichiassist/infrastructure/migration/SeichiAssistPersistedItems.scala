package com.github.unchama.seichiassist.infrastructure.migration

import cats.effect.IO
import com.github.unchama.itemmigration.ItemMigration.ItemConversion
import com.github.unchama.itemmigration.ItemMigrationTarget

object SeichiAssistPersistedItems extends ItemMigrationTarget[IO] {

  override def runMigration(conversion: ItemConversion): IO[Unit] = {
    ???
  }

}
