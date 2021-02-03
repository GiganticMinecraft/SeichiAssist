package com.github.unchama.datarepository.template

import cats.{Applicative, Monad}

import java.util.UUID

/**
 * データレポジトリの初期化処理を記述するオブジェクト。
 *
 * マインクラフトのサーバーの仕様として、プレーヤーがサーバーに実際に参加する前に
 * プレーヤーのUUID/名前ペアを受け取れることになっている。
 *
 * このオブジェクトが記述するのは、そのような状況下において
 *  - ログイン処理後にUUID/名前を受け取り、中間データを作成する
 *  - 実際に [[Player]] のインスタンスが得られ次第 [[R]] を生成する
 *
 * ようなデータリポジトリの処理である。
 */
trait TwoPhasedRepositoryInitialization[F[_], Player, R] {

  type IntermediateData

  /**
   * 参加したプレーヤーのUUID/名前から[[IntermediateData]]を生成する作用。
   *
   * [[IntermediateData]] が何らかの理由により生成できなかった場合、[[PrefetchResult.Failed]]を返す可能性がある。
   */
  val prefetchIntermediateValue: (UUID, String) => F[PrefetchResult[IntermediateData]]

  val prepareData: (Player, IntermediateData) => F[R]

  import cats.implicits._

  def extendPreparation[S](f: Player => R => F[S])(implicit F: Monad[F]): TwoPhasedRepositoryInitialization[F, Player, S] =
    new TwoPhasedRepositoryInitialization[F, Player, S] {
      type I = TwoPhasedRepositoryInitialization.this.IntermediateData

      override type IntermediateData = I
      override val prefetchIntermediateValue: (UUID, String) => F[PrefetchResult[I]] =
        TwoPhasedRepositoryInitialization.this.prefetchIntermediateValue
      override val prepareData: (Player, I) => F[S] =
        (player, i) => TwoPhasedRepositoryInitialization.this.prepareData(player, i).flatMap(f(player))
    }
}

object TwoPhasedRepositoryInitialization {
  def augment[F[_], Player, R, T](singlePhasedRepositoryInitialization: SinglePhasedRepositoryInitialization[F, T])
                                 (prepareFinalData: (Player, T) => F[R]): TwoPhasedRepositoryInitialization[F, Player, R] = {
    new TwoPhasedRepositoryInitialization[F, Player, R] {
      override type IntermediateData = T
      override val prefetchIntermediateValue: (UUID, String) => F[PrefetchResult[T]] =
        singlePhasedRepositoryInitialization.prepareData
      override val prepareData: (Player, T) => F[R] = prepareFinalData
    }
  }

  def canonicallyFrom[
    F[_] : Applicative, Player, R
  ](initialization: SinglePhasedRepositoryInitialization[F, R]): TwoPhasedRepositoryInitialization[F, Player, R] =
    augment(initialization) { case (_, r) => Applicative[F].pure(r) }

  def withoutPrefetching[
    F[_] : Applicative, Player, R
  ](f: Player => F[R]): TwoPhasedRepositoryInitialization[F, Player, R] =
    augment(SinglePhasedRepositoryInitialization.constant(())) { case (player, _) => f(player) }
}
