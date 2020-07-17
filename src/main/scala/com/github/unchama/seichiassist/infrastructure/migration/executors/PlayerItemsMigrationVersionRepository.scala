package com.github.unchama.seichiassist.infrastructure.migration.executors

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData

class PlayerItemsMigrationVersionRepository extends ItemMigrationVersionRepository[IO, PlayerInventoriesData] {
  override type PersistenceLock[TInstance <: Singleton with PlayerInventoriesData] = Nothing

  override def lockVersionPersistence(target: PlayerInventoriesData): Resource[IO, PersistenceLock[target.type]] = ???

  override def getVersionsAppliedTo(target: PlayerInventoriesData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = ???

  override def persistVersionsAppliedTo(target: PlayerInventoriesData, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] = ???
}

