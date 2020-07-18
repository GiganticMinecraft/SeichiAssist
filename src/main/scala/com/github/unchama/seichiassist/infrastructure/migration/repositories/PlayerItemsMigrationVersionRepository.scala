package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import scalikejdbc._

class PlayerItemsMigrationVersionRepository(serverId: String) extends ItemMigrationVersionRepository[IO, PlayerInventoriesData] {
  /**
   * プレーヤーは単一サーバーに1人しか存在しないためロックは不要
   */
  override type PersistenceLock[TInstance <: PlayerInventoriesData] = Any

  override def lockVersionPersistence(target: PlayerInventoriesData): Resource[IO, PersistenceLock[target.type]] = {
    Resource.pure[IO, Any](())
  }

  override def getVersionsAppliedTo(target: PlayerInventoriesData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] = _ => IO {
    implicit val session: DBSession = AutoSession

    val versions = DB readOnly { implicit session =>
      sql"""
        select version_string from seichiassist.player_in_server_item_migration
          where server_id = $serverId and player_uuid = ${target.player.getUniqueId}
      """.map { rs => rs.string("version_string") }.list.apply()
    }

    versions.flatMap(ItemMigrationVersionNumber.fromString).toSet
  }

  override def persistVersionsAppliedTo(target: PlayerInventoriesData,
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] = _ => IO {
    implicit val session: DBSession = AutoSession

    val batchParams = versions.map(version => Seq(ItemMigrationVersionNumber.convertToString(version)))

    DB localTx { implicit session =>
      sql"""
        insert into seichiassist.player_in_server_item_migration(player_uuid, server_id, version_string, completed_at)
        values (${target.player.getUniqueId}, $serverId, ?, cast(now() as datetime))
      """
        .batch(batchParams.toSeq: _*)
        .apply[List]()
    }
  }
}

