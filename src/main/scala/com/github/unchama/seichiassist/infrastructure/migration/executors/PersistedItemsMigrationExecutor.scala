package com.github.unchama.seichiassist.infrastructure.migration.executors

import cats.effect.{Bracket, IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, VersionedItemMigrationExecutor}
import com.github.unchama.seichiassist.infrastructure.migration.executors.PersistedItemsMigrationExecutor.PI
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems

class PersistedItemsMigrationExecutor extends VersionedItemMigrationExecutor[IO, PI] {
  override type PersistenceLock[TInstance <: Singleton with PI] = Nothing

  override implicit val F: Bracket[IO, Throwable] = implicitly

  override def lockVersionPersistence(target: PI): Resource[IO, PersistenceLock[PI]] = ???

  override def getVersionsAppliedTo(target: PI): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = ???

  override def persistVersionsAppliedTo(target: PI, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[PI] => IO[Unit] = ???
}

object PersistedItemsMigrationExecutor {
  private type PI = SeichiAssistPersistedItems.type
}
