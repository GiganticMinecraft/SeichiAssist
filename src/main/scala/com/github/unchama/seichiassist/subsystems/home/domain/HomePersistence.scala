package com.github.unchama.seichiassist.subsystems.home.domain

import cats.{Functor, Monad}
import com.github.unchama.seichiassist.subsystems.home.domain.OperationResult.RenameResult

import java.util.UUID

/**
 * ホームの永続化された情報。
 *
 * この情報はサーバー間で共有されることを想定していない。 例えばサーバー(s1,
 * s2)、プレーヤーのUUID(u)があった時、s1でlist(u)をした結果とs2でlist(u)をした結果は一般に異なる。
 * これは、ホームをサーバー間で共有しないという仕様に基づくものである。
 */
trait HomePersistence[F[_]] {

  import cats.implicits._

  /**
   * 指定したUUIDのプレーヤーが現在のサーバーにて設定しているすべてのホームを取得する。
   */
  def list(ownerUuid: UUID): F[Map[HomeId, Home]]

  /**
   * ホームを登録する。idの範囲などのバリデーションはしない。
   *
   * すでにホームが指定されたidで存在した場合、ホームを上書きする。
   */
  def upsert(ownerUuid: UUID, id: HomeId)(home: Home): F[Unit]

  /**
   * ホームを削除する。
   */
  def remove(ownerUuid: UUID, id: HomeId): F[Boolean]

  /**
   * 所有者のUUIDとホームのIDから単一のホームを取得する。
   */
  final def get(ownerUuid: UUID, id: HomeId)(implicit F: Functor[F]): F[Option[Home]] =
    list(ownerUuid).map(_.get(id))

  /**
   * 指定されたidのホームを登録する。idの範囲などのバリデーションはしない。
   *
   * ホームがすでに存在した場合、古いホームの名前を新しいホームへと引き継ぐ。
   */
  final def upsertLocation(ownerUuid: UUID, id: HomeId)(
    location: HomeLocation
  )(implicit F: Monad[F]): F[Unit] =
    for {
      old <- get(ownerUuid, id)
      newHome = Home(old.flatMap(_.name), location)
      _ <- upsert(ownerUuid, id)(newHome)
    } yield ()

  /**
   * 指定されたホームをnon-atomicに更新する。存在しないホームが指定された場合何も行わない。
   *
   * 作用の結果として更新が行われたかどうかを示すBooleanを返す。
   */
  final def alter(ownerUuid: UUID, id: HomeId)(
    f: Home => Home
  )(implicit F: Monad[F]): F[Boolean] =
    for {
      current <- get(ownerUuid, id)
      _ <- current match {
        case Some(currentHome) => upsert(ownerUuid, id)(f(currentHome))
        case None              => F.unit
      }
    } yield current.nonEmpty

  /**
   * 指定されたホームをnon-atomicにリネームする。存在しないホームが指定された場合何も行わない。
   */
  final def rename(ownerUuid: UUID, id: HomeId)(
    newName: String
  )(implicit F: Monad[F]): F[OperationResult.RenameResult] =
    alter(ownerUuid, id)(_.copy(name = Some(newName))).map { r =>
      if (r) RenameResult.Done else RenameResult.NotFound
    }

}
