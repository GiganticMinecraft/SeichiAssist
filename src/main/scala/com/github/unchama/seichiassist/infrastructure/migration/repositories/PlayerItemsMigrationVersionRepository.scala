package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import scalikejdbc._

class PlayerItemsMigrationVersionRepository(serverId: String) extends ItemMigrationVersionRepository[IO, PlayerInventoriesData] {
  override type PersistenceLock[TInstance <: PlayerInventoriesData] = DBSession

  override def lockVersionPersistence(target: PlayerInventoriesData): Resource[IO, PersistenceLock[target.type]] = {
    /**
     * プレーヤーは単一サーバーに1人しか存在しないためロックは不要
     */
    Resource.make(
      IO {
        AutoSession
      }
    )(
      _ => IO.unit
    )
  }

  override def getVersionsAppliedTo(target: PlayerInventoriesData): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] =
    implicit session => IO {
      sql"""
        select version_string from seichiassist.player_in_server_item_migration
          where server_id = $serverId and player_uuid = ${target.player.getUniqueId.toString}
      """
        .map { rs => rs.string("version_string") }
        .list.apply()
        .flatMap(ItemMigrationVersionNumber.fromString).toSet
    }

  override def persistVersionsAppliedTo(target: PlayerInventoriesData,
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => IO[Unit] =
    implicit session => IO {
      val batchParams = versions.map { version =>
        Seq(target.player.getUniqueId.toString, serverId, version.versionString)
      }

      sql"""
        insert into seichiassist.player_in_server_item_migration(player_uuid, server_id, version_string, completed_at)
        values (?, ?, ?, cast(now() as datetime))
      """
        .batch(batchParams.toSeq: _*)
        .apply[List]()
    }
}

