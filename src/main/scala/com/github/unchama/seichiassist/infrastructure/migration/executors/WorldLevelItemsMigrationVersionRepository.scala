package com.github.unchama.seichiassist.infrastructure.migration.executors

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.WorldLevelData

class WorldLevelItemsMigrationVersionRepository extends ItemMigrationVersionRepository[IO, WorldLevelData] {
  override type PersistenceLock[TInstance <: WorldLevelData] = Nothing

  override def lockVersionPersistence(target: WorldLevelData): Resource[IO, PersistenceLock[target.type]] = ???

  override def getVersionsAppliedTo(target: WorldLevelData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = ???

  override def persistVersionsAppliedTo(target: WorldLevelData, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] = ???
}
