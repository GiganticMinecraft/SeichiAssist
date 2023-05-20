package com.github.unchama.datarepository.template.finalization

import cats.{Applicative, FlatMap}

/**
 * データレポジトリの終了処理を記述するオブジェクト。
 *
 * このオブジェクトが記述するのは、
 *   - 定期的、又は不定期的にプレーヤーのデータを永続化層に書き込み
 *   - 必要ならば、プレーヤーが退出する際に永続化層に書き込む処理とは別にクリーンアップを行う
 *
 * ようなデータレポジトリの処理である。
 */
trait RepositoryFinalization[F[_], Player, R] { self =>

  val persistPair: (Player, R) => F[Unit]

  val finalizeBeforeUnload: (Player, R) => F[Unit]

  import cats.implicits._

  /**
   * 永続化前に `beforePersisting` を、終了処理前に `beforeFinalization` を実行するような終了処理を定義する。
   */
  def withIntermediateEffects[S](beforePersisting: S => F[R])(
    beforeFinalization: S => F[R]
  )(implicit F: FlatMap[F]): RepositoryFinalization[F, Player, S] =
    new RepositoryFinalization[F, Player, S] {
      override val persistPair: (Player, S) => F[Unit] =
        (p, s) => beforePersisting(s).flatMap(r => self.persistPair(p, r))
      override val finalizeBeforeUnload: (Player, S) => F[Unit] =
        (p, s) => beforeFinalization(s).flatMap(r => self.finalizeBeforeUnload(p, r))
    }

  def contraMap[S](sr: S => R): RepositoryFinalization[F, Player, S] =
    new RepositoryFinalization[F, Player, S] {
      override val persistPair: (Player, S) => F[Unit] = (p, s) => self.persistPair(p, sr(s))
      override val finalizeBeforeUnload: (Player, S) => F[Unit] = (p, s) =>
        self.finalizeBeforeUnload(p, sr(s))
    }

  def contraMapKey[K](kp: K => Player): RepositoryFinalization[F, K, R] =
    new RepositoryFinalization[F, K, R] {
      override val persistPair: (K, R) => F[Unit] = (k, r) => self.persistPair(kp(k), r)
      override val finalizeBeforeUnload: (K, R) => F[Unit] = (k, r) =>
        self.finalizeBeforeUnload(kp(k), r)
    }

  def withIntermediateEffect[S](sFr: S => F[R])(
    implicit F: FlatMap[F]
  ): RepositoryFinalization[F, Player, S] =
    withIntermediateEffects(sFr)(sFr)

}

object RepositoryFinalization {

  def withoutAnyPersistence[F[_]: Applicative, Player, R](
    finalization: (Player, R) => F[Unit]
  ): RepositoryFinalization[F, Player, R] =
    new RepositoryFinalization[F, Player, R] {
      override val persistPair: (Player, R) => F[Unit] = (_, _) => Applicative[F].unit
      override val finalizeBeforeUnload: (Player, R) => F[Unit] = finalization
    }

  def withoutAnyFinalization[F[_]: Applicative, Player, R](
    persist: (Player, R) => F[Unit]
  ): RepositoryFinalization[F, Player, R] =
    new RepositoryFinalization[F, Player, R] {
      override val persistPair: (Player, R) => F[Unit] = persist
      override val finalizeBeforeUnload: (Player, R) => F[Unit] = (_, _) => Applicative[F].unit
    }

  def trivial[F[_]: Applicative, Player, R]: RepositoryFinalization[F, Player, R] =
    withoutAnyPersistence((_, _) => Applicative[F].unit)

}
