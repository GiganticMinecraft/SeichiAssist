package com.github.unchama.seichiassist.infrastructure.migration.executors

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.seichiassist.infrastructure.migration.executors.PersistedItemsMigrationVersionRepository.PI
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems

class PersistedItemsMigrationVersionRepository extends ItemMigrationVersionRepository[IO, PI] {
  override type PersistenceLock[TInstance <: Singleton with PI] = Nothing

  override def lockVersionPersistence(target: PI): Resource[IO, PersistenceLock[PI]] = ???

  override def getVersionsAppliedTo(target: PI): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = ???

  override def persistVersionsAppliedTo(target: PI, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[PI] => IO[Unit] = ???
}

object PersistedItemsMigrationVersionRepository {
  private type PI = SeichiAssistPersistedItems.type
}
