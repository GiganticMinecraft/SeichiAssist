package com.github.unchama.seichiassist.infrastructure.migration.persistenceproviders

import java.util.UUID

import cats.effect.IO
import com.github.unchama.itemmigration.service.ItemMigrationPersistence

object PlayerItemMigrationPersistence {

  def persistence(): ItemMigrationPersistence[IO, UUID] = ???

}
