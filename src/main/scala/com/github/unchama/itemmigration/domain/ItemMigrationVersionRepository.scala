package com.github.unchama.itemmigration.domain

import cats.effect.Resource

/**
 * 対象 `T` に関するアイテムマイグレーションの適用済みバージョンのリポジトリ。
 *
 * @tparam F 計算を行うコンテキスト
 * @tparam T このオブジェクトがマイグレーションを実行できるオブジェクト達の型の上界
 */
trait ItemMigrationVersionRepository[F[_], -T <: ItemMigrationTarget[F]] {

  /**
   * `T` の値である `TInstance` に適用されたマイグレーションバージョンの記録のロック
   */
  type PersistenceLock[TInstance <: T]

  /**
   * `target` に適用されたマイグレーションの記録にロックを掛けるための `Resource`。
   */
  def lockVersionPersistence(target: T): Resource[F, PersistenceLock[target.type]]

  /**
   * `target` にすでに適用されたマイグレーションの番号の集合を取得する計算。
   */
  def getVersionsAppliedTo(target: T): PersistenceLock[target.type] => F[Set[ItemMigrationVersionNumber]]

  /**
   * `target` に適用したバージョンを永続化する計算。
   */
  def persistVersionsAppliedTo(target: T, versions: Iterable[ItemMigrationVersionNumber]): PersistenceLock[target.type] => F[Unit]

}
