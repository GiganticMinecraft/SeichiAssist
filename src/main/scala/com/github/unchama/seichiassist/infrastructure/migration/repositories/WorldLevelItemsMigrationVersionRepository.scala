package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{Resource, Sync}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.WorldLevelData
import scalikejdbc._

class WorldLevelItemsMigrationVersionRepository[F[_]](serverId: String)(implicit F: Sync[F])
  extends ItemMigrationVersionRepository[F, WorldLevelData[F]] {

  override type PersistenceLock[TInstance <: WorldLevelData[F]] = Unit

  override def lockVersionPersistence(target: WorldLevelData[F]): Resource[F, PersistenceLock[target.type]] = {
    /**
     * サーバーIDが一意なら更新が一サーバーIDに対して一個しか走らないためロックは不要
     */
    Resource.pure[F, Unit](())
  }

  override def getVersionsAppliedTo(target: WorldLevelData[F]): PersistenceLock[target.type] => F[Set[ItemMigrationVersionNumber]] =
    _ => F.delay {
      DB.localTx { implicit session =>
        sql"""
          select version_string from seichiassist.item_migration_in_server_world_levels where server_id = $serverId
        """
          .map { rs => rs.string("version_string") }
          .list.apply()
          .flatMap(ItemMigrationVersionNumber.fromString).toSet
      }
    }

  override def persistVersionsAppliedTo(target: WorldLevelData[F],
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => F[Unit] =
    _ => {
      val batchParams = versions.map { version =>
        Seq(serverId, version.versionString)
      }.toSeq

      F.delay {
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
