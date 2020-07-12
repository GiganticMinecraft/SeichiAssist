package com.github.unchama.seichiassist.infrastructure.migration.persistenceproviders

import cats.effect.IO
import com.github.unchama.itemmigration.ItemMigrationPersistence
import com.github.unchama.seichiassist.infrastructure.migration.SeichiAssistPersistedItems

/**
 * 永続化されたアイテムに対するマイグレーションの記録を永続化するサービスの実装を与えるオブジェクト。
 */
object PersistedItemMigrationPersistence {

  def provider(): ItemMigrationPersistence.Provider[IO, SeichiAssistPersistedItems.type] = ???

}
