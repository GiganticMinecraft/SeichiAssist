package com.github.unchama.generic.effect

import cats.effect.concurrent.MVar
import cats.effect.{Bracket, Concurrent, ExitCase, Sync}

final class Mutex[F[_], A] private (mVar: MVar[F, A])
                                   (implicit fBracket: Bracket[F, Throwable]) {
  import cats.implicits._

  /**
   *  - 変数のロックを取得
   *  - `use`により新たな値を計算
   *  - 変数を更新
   *  したうえで、`use` の計算結果を反映する `B` を返却するような計算。
   *
   *  `use` が失敗した場合、外側の計算も失敗するが、変数は元の状態へ戻ることが保証される。
   *  また、複数の並行するプロセスがこの計算を行う場合、
   *  ロック取得から変数更新の間にあるプロセスはどの時点でもただ一つしか存在しないことが保証され、
   *  したがってロックを取得できるまで外側の計算が (意味論的な) ブロッキングを行うことになる。
   */
  def lockAndModify[B](use: A => F[(A, B)]): F[B] =
    Bracket[F, Throwable].bracketCase(mVar.take) { a =>
      use(a) >>= { case (newA, b) => mVar.put(newA).as(b) }
    } {
      case (_, ExitCase.Completed) =>
        // このケースではmVarに値はputされている
        ().pure
      case (a, _) =>
        // 元の値をputし直すことでロックを返却する
        // 外側の `F` は失敗する
        mVar.put(a)
    }
}

object Mutex {
  import cats.implicits._

  def of[F[_]: Sync, G[_]: Concurrent, A](initial: A): F[Mutex[G, A]] =
    MVar.in[F, G, A](initial) >>= (mv => Sync[F].delay(new Mutex(mv)))
}
