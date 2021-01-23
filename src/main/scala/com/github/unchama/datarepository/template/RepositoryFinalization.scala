package com.github.unchama.datarepository.template

import cats.Monad

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

  def contraFlatMap[S](sFr: S => F[R])(implicit F: Monad[F]): RepositoryFinalization[F, Player, S] =
    new RepositoryFinalization[F, Player, S] {
      override val persistPair: (Player, S) => F[Unit] =
        (p, s) => sFr(s).flatMap(r => RepositoryFinalization.this.persistPair(p, r))
      override val finalizeBeforeUnload: (Player, S) => F[Unit] =
        (p, s) => sFr(s).flatMap(r => RepositoryFinalization.this.finalizeBeforeUnload(p, r))
    }

}
