package com.github.unchama.seichiassist.infrastructure.migration.executors

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.seichiassist.infrastructure.migration.executors.PersistedItemsMigrationVersionRepository.PersistedItems
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems

class PersistedItemsMigrationVersionRepository extends ItemMigrationVersionRepository[IO, PersistedItems] {
  override type PersistenceLock[TInstance <: PersistedItems] = Nothing

  override def lockVersionPersistence(target: PersistedItems): Resource[IO, PersistenceLock[PersistedItems]] = ???

  override def getVersionsAppliedTo(target: PersistedItems): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = ???

  override def persistVersionsAppliedTo(target: PersistedItems,
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[PersistedItems] => IO[Unit] = ???
}

object PersistedItemsMigrationVersionRepository {
  private type PersistedItems = SeichiAssistPersistedItems.type
}
