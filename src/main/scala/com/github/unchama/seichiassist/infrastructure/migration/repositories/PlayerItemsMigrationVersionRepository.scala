package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{Resource, Sync}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import scalikejdbc._

class PlayerItemsMigrationVersionRepository[F[_]](serverId: String)(implicit F: Sync[F])
  extends ItemMigrationVersionRepository[F, PlayerInventoriesData[F]] {

  override type PersistenceLock[TInstance <: PlayerInventoriesData[F]] = Unit

  override def lockVersionPersistence(target: PlayerInventoriesData[F]): Resource[F, PersistenceLock[target.type]] = {
    /**
     * プレーヤーは単一サーバーに1人しか存在しないためロックは不要
     */
    Resource.pure[F, Unit](())
  }

  override def getVersionsAppliedTo(target: PlayerInventoriesData[F]): PersistenceLock[target.type] => F[Set[ItemMigrationVersionNumber]] =
    _ => F.delay {
      DB.localTx { implicit session =>
        sql"""
          select version_string from seichiassist.player_in_server_item_migration
            where server_id = $serverId and player_uuid = ${target.player.getUniqueId.toString}
        """
          .map { rs => rs.string("version_string") }
          .list.apply()
          .flatMap(ItemMigrationVersionNumber.fromString).toSet

      }
    }

  override def persistVersionsAppliedTo(target: PlayerInventoriesData[F],
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => F[Unit] =
    _ => F.delay {
      val batchParams = versions.map { version =>
        Seq(target.player.getUniqueId.toString, serverId, version.versionString)
      }

      DB.localTx { implicit session =>
        sql"""
          insert into seichiassist.player_in_server_item_migration(player_uuid, server_id, version_string, completed_at)
          values (?, ?, ?, cast(now() as datetime))
        """
          .batch(batchParams.toSeq: _*)
          .apply[List]()
      }
    }
}

