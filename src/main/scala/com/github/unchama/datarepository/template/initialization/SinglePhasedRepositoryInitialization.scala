package com.github.unchama.datarepository.template.initialization

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.{Applicative, Functor, Monad}

import java.util.UUID

/**
 * データレポジトリの初期化処理を記述するオブジェクト。
 *
 * マインクラフトのサーバーの仕様として、プレーヤーがサーバーに実際に参加する前に プレーヤーのUUID/名前ペアを受け取れることになっている。
 *
 * このオブジェクトが記述するのは、そのような状況下において
 *   - ログイン処理後にUUID/名前を受け取り次第 [[R]] を生成する
 *
 * ようなデータリポジトリの処理である。
 */
trait SinglePhasedRepositoryInitialization[F[_], R] {

  /**
   * 参加したプレーヤーのUUID/名前から[[R]]を生成する作用。
   *
   * [[R]] が何らかの理由により生成できなかった場合、[[PrefetchResult.Failed]]を返す可能性がある。
   */
  def prepareData(uuid: UUID, name: String): F[PrefetchResult[R]]

  import cats.implicits._

  def extendPreparation[S](f: (UUID, String) => R => F[S])(
    implicit F: Monad[F]
  ): SinglePhasedRepositoryInitialization[F, S] =
    (uuid, name) => prepareData(uuid, name) >>= (_.traverse(f(uuid, name)))

}

object SinglePhasedRepositoryInitialization {

  import cats.implicits._

  implicit def functorInstance[F[_]: Functor]
    : Functor[SinglePhasedRepositoryInitialization[F, *]] =
    new Functor[SinglePhasedRepositoryInitialization[F, *]] {
      override def map[A, B](fa: SinglePhasedRepositoryInitialization[F, A])(
        f: A => B
      ): SinglePhasedRepositoryInitialization[F, B] =
        (uuid, name) => fa.prepareData(uuid, name).map(_.map(f))
    }

  def withSupplier[F[_]: Functor, R](fr: F[R]): SinglePhasedRepositoryInitialization[F, R] =
    (_, _) => Functor[F].map(fr)(PrefetchResult.Success.apply)

  def constant[F[_]: Applicative, R](v: R): SinglePhasedRepositoryInitialization[F, R] =
    withSupplier(Applicative[F].pure(v))

  def forRefCell[F[_]: Sync, R](
    initialization: SinglePhasedRepositoryInitialization[F, R]
  ): SinglePhasedRepositoryInitialization[F, Ref[F, R]] =
    (uuid, name) => initialization.prepareData(uuid, name) >>= (_.traverse(Ref[F].of(_)))

}
