package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.{ExitCase, Resource, Sync}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems
import scalikejdbc._

class PersistedItemsMigrationVersionRepository[F[_]](implicit dbSession: DBSession, F: Sync[F])
  extends ItemMigrationVersionRepository[F, SeichiAssistPersistedItems[F]] {

  private type PersistedItems = SeichiAssistPersistedItems[F]

  override type PersistenceLock[TInstance <: PersistedItems] = Unit

  override def lockVersionPersistence(target: PersistedItems): Resource[F, PersistenceLock[PersistedItems]] = {
    Resource.makeCase(F.delay {
      // トランザクション開始がここになる
      // https://dev.mysql.com/doc/refman/5.6/ja/lock-tables-and-transactions.html
      sql"set autocommit=0".update().apply()

      // ロックを取得するときは利用するテーブルすべてをロックしなければならない
      sql"lock tables seichiassist.item_migration_on_database write, seichiassist.playerdata write".update().apply()

      // このリソースを使用する際にはロックが取れているというのを保証すればよいため、リソースの実体は無くて良い
      ()
    }) {
      case (_, ExitCase.Completed) =>
        F.delay {
          sql"commit".update().apply()
          sql"unlock tables".update().apply()
        }
      case _ =>
        F.delay {
          sql"rollback".update().apply()
          sql"unlock tables".update().apply()
        }
    }
  }

  override def getVersionsAppliedTo(target: PersistedItems): PersistenceLock[target.type] => F[Set[ItemMigrationVersionNumber]] =
    _ => F.delay {
      sql"""
        select version_string from seichiassist.item_migration_on_database
      """
        .map { rs => rs.string("version_string") }
        .list.apply()
        .flatMap(ItemMigrationVersionNumber.fromString).toSet
    }

  override def persistVersionsAppliedTo(target: PersistedItems,
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[PersistedItems] => F[Unit] =
    _ => F.delay {
      val batchParams = versions.map(version => Seq(version.versionString))

      sql"""
        insert into seichiassist.item_migration_on_database(version_string, completed_at)
        values (?, cast(now() as datetime))
      """
        .batch(batchParams.toSeq: _*)
        .apply[List]()
    }
}
