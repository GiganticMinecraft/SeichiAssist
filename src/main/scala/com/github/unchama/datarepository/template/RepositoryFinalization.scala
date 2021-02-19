package com.github.unchama.datarepository.template

import cats.effect.concurrent.Ref
import cats.{Applicative, FlatMap}

/**
 * データレポジトリの終了処理を記述するオブジェクト。
 *
 * このオブジェクトが記述するのは、
 *  - 定期的、又は不定期的にプレーヤーのデータを永続化層に書き込み
 *  - 必要ならば、プレーヤーが退出する際に永続化層に書き込む処理とは別にクリーンアップを行う
 *
 * ようなデータレポジトリの処理である。
 */
trait RepositoryFinalization[F[_], Player, R] {

  val persistPair: (Player, R) => F[Unit]

  val finalizeBeforeUnload: (Player, R) => F[Unit]

  import cats.implicits._

  /**
   * 永続化前に `beforePersisting` を、終了処理前に `beforeFinalization` を実行するような終了処理を定義する。
   */
  def contraBiFlatMap[S](beforePersisting: S => F[R])
                        (beforeFinalization: S => F[R])
                        (implicit F: FlatMap[F]): RepositoryFinalization[F, Player, S] =
    new RepositoryFinalization[F, Player, S] {
      override val persistPair: (Player, S) => F[Unit] =
        (p, s) => beforePersisting(s).flatMap(r => RepositoryFinalization.this.persistPair(p, r))
      override val finalizeBeforeUnload: (Player, S) => F[Unit] =
        (p, s) => beforeFinalization(s).flatMap(r => RepositoryFinalization.this.finalizeBeforeUnload(p, r))
    }

  def contraFlatMap[S](sFr: S => F[R])(implicit F: FlatMap[F]): RepositoryFinalization[F, Player, S] =
    contraBiFlatMap(sFr)(sFr)

}

object RepositoryFinalization {

  def withoutAnyPersistence[
    F[_] : Applicative, Player, R
  ](finalization: (Player, R) => F[Unit]): RepositoryFinalization[F, Player, R] =
    new RepositoryFinalization[F, Player, R] {
      override val persistPair: (Player, R) => F[Unit] = (_, _) => Applicative[F].unit
      override val finalizeBeforeUnload: (Player, R) => F[Unit] = finalization
    }

  def liftToRefFinalization[
    F[_] : FlatMap, Player, R
  ](finalization: RepositoryFinalization[F, Player, R]): RepositoryFinalization[F, Player, Ref[F, R]] =
    finalization.contraFlatMap[Ref[F, R]](_.get)

}
