package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.WorldLevelData
import scalikejdbc.{AutoSession, DB, DBSession, _}

class WorldLevelItemsMigrationVersionRepository(serverId: String) extends ItemMigrationVersionRepository[IO, WorldLevelData] {
  /**
   * サーバーIDが一意なら更新が一サーバーIDに対して一個しか走らないためロックは不要
   */
  override type PersistenceLock[TInstance <: WorldLevelData] = Unit

  override def lockVersionPersistence(target: WorldLevelData): Resource[IO, PersistenceLock[target.type]] = {
    Resource.pure[IO, Unit](())
  }

  override def getVersionsAppliedTo(target: WorldLevelData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = _ => IO {
    implicit val session: DBSession = AutoSession

    val versions = DB readOnly { implicit session =>
      sql"""
        select version_string from seichiassist.item_migration_in_server_world_levels where server_id = $serverId
      """.map { rs => rs.string("version_string") }.list.apply()
    }

    versions.flatMap(ItemMigrationVersionNumber.fromString).toSet
  }

  override def persistVersionsAppliedTo(target: WorldLevelData, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] = _ => IO {
    implicit val session: DBSession = AutoSession

    val batchParams = versions.map(version => Seq(ItemMigrationVersionNumber.convertToString(version)))

    DB localTx { implicit session =>
      sql"""
        insert into seichiassist.item_migration_in_server_world_levels(server_id, version_string, completed_at)
        values ($serverId, ?, cast(now() as datetime))
      """
        .batch(batchParams.toSeq: _*)
        .apply[List]()
    }
  }
}
