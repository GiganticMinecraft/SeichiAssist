package com.github.unchama.itemmigration

import cats.Monad
import cats.effect.Resource
import com.github.unchama.itemmigration.ItemMigration.VersionNumber

trait ItemMigrationPersistence[F[_]] {
  implicit val fMonad: Monad[F]

  /**
   * 既に完了した変換のバージョンのリストを取得する。
   */
  def getCompletedVersions: F[IndexedSeq[VersionNumber]]

  /**
   * 与えられたマイグレーションの集まりのうち、
   * 実行が必要なマイグレーションの集まりを Iterable として返す。
   *
   * デフォルトの実装では、例えばバージョンが1.0.0, 1.0.1, 1.1.0であるマイグレーション列が与えられたにも関わらず
   * 永続化された結果が1.0.0, 1.1.0であった場合、即ち永続化されていたものに欠番があった場合は、
   * このメソッドの結果はその欠番(ここでは1.0.1)を含む結果を返し、エラーにはしない。
   */
  def filterRequiredMigrations(migrations: IndexedSeq[ItemMigration]): F[IndexedSeq[ItemMigration]] = {
    import cats.implicits._

    for {
      completedVersions <- getCompletedVersions
      completedVersionSet = completedVersions.toSet
    } yield migrations.filter(m => !completedVersionSet.contains(m.version))
  }

  /**
   * 完了した変換のバージョンを永続化する。
   */
  def writeCompletedVersion(version: VersionNumber): F[Unit]

  def writeCompletedMigrations(versions: IndexedSeq[ItemMigration]): F[Unit] = {
    import cats.implicits._

    versions.map(_.version).toList.traverse(writeCompletedVersion).as(())
  }
}

trait ItemMigrationPersistenceProvider[F[_]] {
  /**
   * 必要ならば永続化層のロックを取り、永続化層へのアクセスを提供するような `Resource` を返す。
   */
  def withPersistence: Resource[F, ItemMigrationPersistence[F]]
}
