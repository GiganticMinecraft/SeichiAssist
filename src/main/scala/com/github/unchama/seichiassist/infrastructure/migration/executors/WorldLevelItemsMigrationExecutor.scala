package com.github.unchama.seichiassist.infrastructure.migration.executors

import cats.effect.{Bracket, IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, VersionedItemMigrationExecutor}
import com.github.unchama.itemmigration.targets.WorldLevelData

class WorldLevelItemsMigrationExecutor extends VersionedItemMigrationExecutor[IO, WorldLevelData] {
  override type PersistenceLock[TInstance <: Singleton with WorldLevelData] = Nothing

  override implicit val F: Bracket[IO, Throwable] = implicitly

  override def lockVersionPersistence(target: WorldLevelData): Resource[IO, PersistenceLock[target.type]] = ???

  override def getVersionsAppliedTo(target: WorldLevelData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = ???

  override def persistVersionsAppliedTo(target: WorldLevelData, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] = ???
}
