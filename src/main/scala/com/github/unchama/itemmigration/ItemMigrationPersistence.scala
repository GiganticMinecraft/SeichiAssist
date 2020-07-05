package com.github.unchama.itemmigration

import cats.Monad
import cats.effect.Resource
import com.github.unchama.itemmigration.ItemMigration.VersionNumber

trait ItemMigrationPersistence[F[_], -T] {
  implicit val fMonad: Monad[F]

  /**
   * 既に完了した変換のバージョンのリストを取得する。
   */
  def getCompletedVersions(target: T): F[IndexedSeq[VersionNumber]]

  /**
   * 与えられたマイグレーションの集まりのうち、
   * 実行が必要なマイグレーションの集まりを Iterable として返す。
   *
   * デフォルトの実装では、例えばバージョンが1.0.0, 1.0.1, 1.1.0であるマイグレーション列が与えられたにも関わらず
   * 永続化された結果が1.0.0, 1.1.0であった場合、即ち永続化されていたものに欠番があった場合は、
   * このメソッドの結果はその欠番(ここでは1.0.1)を含む結果を返し、エラーにはしない。
   */
  def filterRequiredMigrations(target: T)(migrations: IndexedSeq[ItemMigration]): F[IndexedSeq[ItemMigration]] = {
    import cats.implicits._

    for {
      completedVersions <- getCompletedVersions(target)
      completedVersionSet = completedVersions.toSet
    } yield migrations.filter(m => !completedVersionSet.contains(m.version))
  }

  /**
   * 完了した変換のバージョンを永続化する。
   */
  def writeCompletedVersion(target: T)(version: VersionNumber): F[Unit]

  def writeCompletedMigrations(target: T)(migrations: IndexedSeq[ItemMigration]): F[Unit] = {
    import cats.implicits._

    migrations.map(_.version).toList.traverse(writeCompletedVersion(target)).as(())
  }
}

object ItemMigrationPersistence {
  /**
   * 必要ならば永続化層のロックを取り、永続化層へのアクセスを提供するような `Resource`
   */
  type Provider[F[_], -T] = Resource[F, ItemMigrationPersistence[F, T]]
}
