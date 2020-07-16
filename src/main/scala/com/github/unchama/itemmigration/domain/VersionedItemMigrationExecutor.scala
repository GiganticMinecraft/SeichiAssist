package com.github.unchama.itemmigration.domain

import cats.effect.{Bracket, Resource}

/**
 * TODO: 契約をちゃんと書く / このtraitのオブジェクトは一体何？
 *
 * @tparam F 計算を行うコンテキスト
 * @tparam T このオブジェクトがマイグレーションを実行できるオブジェクト達の型の上界
 */
trait VersionedItemMigrationExecutor[F[_], -T <: ItemMigrationTarget[F]] {

  /**
   * `T` の値に関するバージョン番号のロック
   */
  type PersistenceLock[TInstance <: Singleton with T]

  implicit val F: Bracket[F, Throwable]

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

  /**
   * `target` に対して、 `migrations` に含まれるマイグレーションのうち、
   * まだ適用されていないものを適用し、適用したことを記録するような計算。
   */
  final def runMigration(migrations: ItemMigrations)(target: T): F[Unit] = {
    import cats.implicits._

    lockVersionPersistence(target).use { implicit lock =>
      for {
        appliedVersions <- getVersionsAppliedTo(target)(lock)
        migrationsToApply = migrations.yetToBeApplied(appliedVersions)
        _ <- target.runMigration(migrationsToApply.toSingleConversion)
        _ <- persistVersionsAppliedTo(target, migrationsToApply.versions)(lock)
      } yield ()
    }
  }

}
