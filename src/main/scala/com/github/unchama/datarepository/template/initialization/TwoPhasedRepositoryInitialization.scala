package com.github.unchama.datarepository.template.initialization

import cats.{Applicative, Functor, Monad}

import java.util.UUID

/**
 * データレポジトリの初期化処理を記述するオブジェクト。
 *
 * マインクラフトのサーバーの仕様として、プレーヤーがサーバーに実際に参加する前に プレーヤーのUUID/名前ペアを受け取れることになっている。
 *
 * このオブジェクトが記述するのは、そのような状況下において
 *   - ログイン処理後にUUID/名前を受け取り、中間データを作成する
 *   - 実際に [[Player]] のインスタンスが得られ次第 [[R]] を生成する
 *
 * ようなデータリポジトリの処理である。
 */
trait TwoPhasedRepositoryInitialization[F[_], Player, R] {
  self =>

  type IntermediateData

  /**
   * 参加したプレーヤーのUUID/名前から[[IntermediateData]]を生成する作用。
   *
   * [[IntermediateData]] が何らかの理由により生成できなかった場合、[[PrefetchResult.Failed]]を返す可能性がある。
   */
  val prefetchIntermediateValue: (UUID, String) => F[PrefetchResult[IntermediateData]]

  val prepareData: (Player, IntermediateData) => F[R]

  import cats.implicits._

  def extendPreparation[S](
    f: Player => R => F[S]
  )(implicit F: Monad[F]): TwoPhasedRepositoryInitialization[F, Player, S] =
    new TwoPhasedRepositoryInitialization[F, Player, S] {
      override type IntermediateData = self.IntermediateData
      override val prefetchIntermediateValue
        : (UUID, String) => F[PrefetchResult[IntermediateData]] =
        self.prefetchIntermediateValue
      override val prepareData: (Player, IntermediateData) => F[S] =
        (player, i) => self.prepareData(player, i).flatMap(f(player))
    }
}

object TwoPhasedRepositoryInitialization {

  import cats.implicits._

  implicit def functorInstance[F[_]: Functor, Player]
    : Functor[TwoPhasedRepositoryInitialization[F, Player, *]] =
    new Functor[TwoPhasedRepositoryInitialization[F, Player, *]] {
      override def map[A, B](
        fa: TwoPhasedRepositoryInitialization[F, Player, A]
      )(f: A => B): TwoPhasedRepositoryInitialization[F, Player, B] =
        new TwoPhasedRepositoryInitialization[F, Player, B] {
          override type IntermediateData = fa.IntermediateData
          override val prefetchIntermediateValue
            : (UUID, String) => F[PrefetchResult[IntermediateData]] =
            fa.prefetchIntermediateValue
          override val prepareData: (Player, IntermediateData) => F[B] =
            (player, i) => fa.prepareData(player, i).map(f)
        }
    }

  def augment[F[_], Player, R, T](
    singlePhasedRepositoryInitialization: SinglePhasedRepositoryInitialization[F, T]
  )(prepareFinalData: (Player, T) => F[R]): TwoPhasedRepositoryInitialization[F, Player, R] = {
    new TwoPhasedRepositoryInitialization[F, Player, R] {
      override type IntermediateData = T
      override val prefetchIntermediateValue: (UUID, String) => F[PrefetchResult[T]] =
        singlePhasedRepositoryInitialization.prepareData
      override val prepareData: (Player, T) => F[R] = prepareFinalData
    }
  }

  def canonicallyFrom[F[_]: Applicative, Player, R](
    initialization: SinglePhasedRepositoryInitialization[F, R]
  ): TwoPhasedRepositoryInitialization[F, Player, R] =
    augment(initialization) { case (_, r) => Applicative[F].pure(r) }

  def withoutPrefetching[F[_]: Applicative, Player, R](
    f: Player => F[R]
  ): TwoPhasedRepositoryInitialization[F, Player, R] =
    augment(SinglePhasedRepositoryInitialization.constant[F, Unit](())) {
      case (player, _) => f(player)
    }
}
