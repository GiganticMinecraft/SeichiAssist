package com.github.unchama.seichiassist.infrastructure.migration.repositories

import cats.effect.ExitCase.Completed
import cats.effect.{ExitCase, IO, Resource}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionNumber, ItemMigrationVersionRepository}
import com.github.unchama.seichiassist.infrastructure.migration.targets.SeichiAssistPersistedItems
import scalikejdbc._

class PersistedItemsMigrationVersionRepository(implicit dbSession: DBSession)
  extends ItemMigrationVersionRepository[IO, SeichiAssistPersistedItems] {

  private type PersistedItems = SeichiAssistPersistedItems

  override type PersistenceLock[TInstance <: PersistedItems] = Unit

  override def lockVersionPersistence(target: PersistedItems): Resource[IO, PersistenceLock[PersistedItems]] = {
    Resource.makeCase(IO {
      // トランザクション開始がここになる
      // https://dev.mysql.com/doc/refman/5.6/ja/lock-tables-and-transactions.html
      sql"set autocommit=0".update().apply()

      // ロックを取得するときは利用するテーブルすべてをロックしなければならない
      sql"lock tables seichiassist.item_migration_on_database write, seichiassist.playerdata write".update().apply()

      // このリソースを使用する際にはロックが取れているというのを保証すればよいため、リソースの実体は無くて良い
      ()
    }) {
      case (_, ExitCase.Completed) =>
        IO {
          sql"commit".update().apply()
          sql"unlock tables".update().apply()
        }
      case _ =>
        IO {
          sql"rollback".update().apply()
          sql"unlock tables".update().apply()
        }
    }
  }

  override def getVersionsAppliedTo(target: PersistedItems): PersistenceLock[target.type] => IO[Set[ItemMigrationVersionNumber]] =
    _ => IO {
      sql"""
        select version_string from seichiassist.item_migration_on_database
      """
        .map { rs => rs.string("version_string") }
        .list.apply()
        .flatMap(ItemMigrationVersionNumber.fromString).toSet
    }

  override def persistVersionsAppliedTo(target: PersistedItems,
                                        versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[PersistedItems] => IO[Unit] =
    _ => IO {
      val batchParams = versions.map(version => Seq(version.versionString))

      sql"""
        insert into seichiassist.item_migration_on_database(version_string, completed_at)
        values (?, cast(now() as datetime))
      """
        .batch(batchParams.toSeq: _*)
        .apply[List]()
    }
}
