package com.github.unchama.seichiassist.infrastructure.migration.executors

import cats.effect.{Bracket, IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, VersionedItemMigrationExecutor}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData

class PlayerItemsMigrationExecutor extends VersionedItemMigrationExecutor[IO, PlayerInventoriesData] {
  override type PersistenceLock[TInstance <: Singleton with PlayerInventoriesData] = Nothing

  override implicit val F: Bracket[IO, Throwable] = implicitly

  override def lockVersionPersistence(target: PlayerInventoriesData): Resource[IO, PersistenceLock[target.type]] = ???

  override def getVersionsAppliedTo(target: PlayerInventoriesData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = ???

  override def persistVersionsAppliedTo(target: PlayerInventoriesData, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] = ???
}

