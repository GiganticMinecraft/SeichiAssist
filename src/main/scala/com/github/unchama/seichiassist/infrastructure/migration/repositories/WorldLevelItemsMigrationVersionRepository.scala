package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.WorldLevelData
import scalikejdbc.{AutoSession, DBSession, _}

class WorldLevelItemsMigrationVersionRepository(serverId: String) extends ItemMigrationVersionRepository[IO, WorldLevelData] {
  override type PersistenceLock[TInstance <: WorldLevelData] = DBSession

  override def lockVersionPersistence(target: WorldLevelData): Resource[IO, PersistenceLock[target.type]] = {
    /**
     * サーバーIDが一意なら更新が一サーバーIDに対して一個しか走らないためロックは不要
     */
    Resource.make(
      IO {
        AutoSession
      }
    )(
      _ => IO.unit
    )
  }

  override def getVersionsAppliedTo(target: WorldLevelData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] =
    implicit session => IO {
      sql"""
        select version_string from seichiassist.item_migration_in_server_world_levels where server_id = $serverId
      """
        .map { rs => rs.string("version_string") }
        .list.apply()
        .flatMap(ItemMigrationVersionNumber.fromString).toSet
    }

  override def persistVersionsAppliedTo(target: WorldLevelData,
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] =
    implicit session => IO {
      val batchParams = versions.map { version =>
        Seq(serverId, version.versionString)
      }.toSeq

      sql"""
        insert into seichiassist.item_migration_in_server_world_levels(server_id, version_string, completed_at)
        values (?, ?, cast(now() as datetime))
      """
        .batch(batchParams: _*)
        .apply[List]()
    }
}
