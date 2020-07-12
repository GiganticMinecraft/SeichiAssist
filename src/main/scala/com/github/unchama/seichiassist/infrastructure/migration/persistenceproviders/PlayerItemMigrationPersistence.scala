package com.github.unchama.seichiassist.infrastructure.migration.persistenceproviders

import java.util.UUID

import cats.effect.IO
import com.github.unchama.itemmigration.ItemMigrationPersistence

object PlayerItemMigrationPersistence {

  def persistence(): ItemMigrationPersistence[IO, UUID] = ???

}
