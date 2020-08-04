package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.WorldLevelData
import scalikejdbc.{AutoSession, DBSession, _}

class WorldLevelItemsMigrationVersionRepository(serverId: String) extends ItemMigrationVersionRepository[IO, WorldLevelData] {
  override type PersistenceLock[TInstance <: WorldLevelData] = Unit

  override def lockVersionPersistence(target: WorldLevelData): Resource[IO, PersistenceLock[target.type]] = {
    /**
     * サーバーIDが一意なら更新が一サーバーIDに対して一個しか走らないためロックは不要
     */
    Resource.pure(())
  }

  override def getVersionsAppliedTo(target: WorldLevelData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] =
    _ => IO {
      DB.localTx { implicit session =>
        sql"""
          select version_string from seichiassist.item_migration_in_server_world_levels where server_id = $serverId
        """
          .map { rs => rs.string("version_string") }
          .list.apply()
          .flatMap(ItemMigrationVersionNumber.fromString).toSet
      }
    }

  override def persistVersionsAppliedTo(target: WorldLevelData,
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] =
    _ => {
      val batchParams = versions.map { version =>
        Seq(serverId, version.versionString)
      }.toSeq

      IO {
        DB.localTx { implicit session =>
          sql"""
          insert into seichiassist.item_migration_in_server_world_levels(server_id, version_string, completed_at)
          values (?, ?, cast(now() as datetime))
        """
            .batch(batchParams: _*)
            .apply[List]()
        }
      }
    }
}
