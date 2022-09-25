package com.github.unchama.seichiassist.subsystems.home.domain

import cats.{Functor, Monad}
import com.github.unchama.seichiassist.subsystems.subhome.domain.OperationResult.RenameResult

import java.util.UUID

/**
 * サブホームの永続化された情報。
 *
 * この情報はサーバー間で共有されることを想定していない。 例えばサーバー(s1,
 * s2)、プレーヤーのUUID(u)があった時、s1でlist(u)をした結果とs2でlist(u)をした結果は一般に異なる。
 * これは、サブホームをサーバー間で共有しないという仕様に基づくものである。
 */
trait SubHomePersistence[F[_]] {

  import cats.implicits._

  /**
   * 指定したUUIDのプレーヤーが現在のサーバーにて設定しているすべてのサブホームを取得する。
   */
  def list(ownerUuid: UUID): F[Map[SubHomeId, SubHome]]

  /**
   * サブホームを登録する。idの範囲などのバリデーションはしない。
   *
   * すでにサブホームが指定されたidで存在した場合、サブホームを上書きする。
   */
  def upsert(ownerUuid: UUID, id: SubHomeId)(subHome: SubHome): F[Unit]

  /**
   * サブホームを削除する。
   */
  def remove(ownerUuid: UUID, id: SubHomeId): F[Boolean]

  /**
   * 所有者のUUIDとサブホームのIDから単一のサブホームを取得する。
   */
  final def get(ownerUuid: UUID, id: SubHomeId)(implicit F: Functor[F]): F[Option[SubHome]] =
    list(ownerUuid).map(_.get(id))

  /**
   * 指定されたidのサブホームを登録する。idの範囲などのバリデーションはしない。
   *
   * サブホームがすでに存在した場合、古いサブホームの名前を新しいサブホームへと引き継ぐ。
   */
  final def upsertLocation(ownerUuid: UUID, id: SubHomeId)(
    location: SubHomeLocation
  )(implicit F: Monad[F]): F[Unit] =
    for {
      old <- get(ownerUuid, id)
      newSubHome = SubHome(old.flatMap(_.name), location)
      _ <- upsert(ownerUuid, id)(newSubHome)
    } yield ()

  /**
   * 指定されたサブホームをnon-atomicに更新する。存在しないサブホームが指定された場合何も行わない。
   *
   * 作用の結果として更新が行われたかどうかを示すBooleanを返す。
   */
  final def alter(ownerUuid: UUID, id: SubHomeId)(
    f: SubHome => SubHome
  )(implicit F: Monad[F]): F[Boolean] =
    for {
      current <- get(ownerUuid, id)
      _ <- current match {
        case Some(currentSubHome) => upsert(ownerUuid, id)(f(currentSubHome))
        case None                 => F.unit
      }
    } yield current.nonEmpty

  /**
   * 指定されたサブホームをnon-atomicにリネームする。存在しないサブホームが指定された場合何も行わない。
   */
  final def rename(ownerUuid: UUID, id: SubHomeId)(
    newName: String
  )(implicit F: Monad[F]): F[OperationResult.RenameResult] =
    alter(ownerUuid, id)(_.copy(name = Some(newName))).map { r =>
      if (r) RenameResult.Done else RenameResult.NotFound
    }

}
