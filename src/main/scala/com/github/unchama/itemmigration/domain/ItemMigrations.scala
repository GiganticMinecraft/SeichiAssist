package com.github.unchama.itemmigration.domain

import org.bukkit.inventory.ItemStack

/**
 * アイテムスタック変換の線形な集まり。
 */
case class ItemMigrations(migrations: IndexedSeq[ItemMigration]) {

  private implicit val versionComponentOrdering: Ordering[ItemMigrationVersionComponent] = {
    Ordering.by(_.value)
  }

  /**
   * @return マイグレーションをバージョン番号でソートした新しい `ItemMigrations`
   */
  def sorted: ItemMigrations = {
    import Ordering.Implicits._

    ItemMigrations(migrations.sortBy(_.version.components.toList.toSeq))
  }

  /**
   * このオブジェクトが持つマイグレーションのうち、
   * `appliedVersions` にバージョンが含まれていないマイグレーションのみを含む新しい `ItemMigrations` を返す。
   */
  def yetToBeApplied(appliedVersions: Set[ItemMigrationVersionNumber]): ItemMigrations =
    ItemMigrations {
      migrations.filter(m => !appliedVersions.contains(m.version))
    }

  /**
   * @return マイグレーション列のアイテムスタック変換関数を順番に全て合成した関数。
   */
  def toSingleConversion: ItemStackConversion = {
    if (migrations.isEmpty) {
      identity[ItemStack]
    } else {
      migrations.map(_.conversion).reduce((c1, c2) => c1.andThen(c2))
    }
  }

  /**
   * このオブジェクトが持つマイグレーションのバージョンのリスト。
   */
  def versions: List[ItemMigrationVersionNumber] = {
    migrations.map(_.version).toList
  }

  def isEmpty: Boolean = migrations.isEmpty

}
