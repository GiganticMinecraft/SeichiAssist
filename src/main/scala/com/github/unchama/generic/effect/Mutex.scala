package com.github.unchama.generic.effect

import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{Bracket, Concurrent, ExitCase, Sync}
import com.github.unchama.generic.ContextCoercion

final class Mutex[
  MutexContext[_],
  ReadContext[_] : ContextCoercion[*[_], MutexContext],
  A
] private(mVar: MVar[MutexContext, A], previous: Ref[ReadContext, A])
         (implicit fBracket: Bracket[MutexContext, Throwable]) {

  import ContextCoercion._
  import cats.implicits._

  /**
   *  - 変数のロックを取得
   *  - `use`により新たな値を計算
   *  - 変数を更新
   *    したうえで、`use` の計算結果を反映する `B` を返却するような計算。
   *
   * `use` が失敗した場合、外側の計算も失敗するが、変数は元の状態へ戻ることが保証される。
   * また、複数の並行するプロセスがこの計算を行う場合、
   * ロック取得から変数更新の間にあるプロセスはどの時点でもただ一つしか存在しないことが保証され、
   * したがってロックを取得できるまで外側の計算が (意味論的な) ブロッキングを行うことになる。
   */
  def lockAndModify[B](use: A => MutexContext[(A, B)]): MutexContext[B] =
    Bracket[MutexContext, Throwable].bracketCase(mVar.take) { a =>
      use(a) >>= { case (newA, b) =>
        mVar
          .put(newA)
          .flatTap(_ => previous.set(newA).coerceTo[MutexContext])
          .as(b)
      }
    } {
      case (_, ExitCase.Completed) =>
        // このケースではmVarに値はputされている
        fBracket.unit
      case (a, _) =>
        // 元の値をputし直すことでロックを返却する
        // 外側の `F` は失敗する
        mVar.put(a)
    }

  /**
   * 最後にsetされた値を排他制御を無視して取得する計算。
   * ロックを取得する必要があるケースでは [[lockAndModify]] を使用すること。
   */
  val readLatest: ReadContext[A] = previous.get
}

object Mutex {

  import cats.implicits._

  def of[
    F[_] : Concurrent,
    G[_] : Sync : ContextCoercion[*[_], F],
    A
  ](initial: A): G[Mutex[F, G, A]] = {
    for {
      ref <- Ref.of[G, A](initial)
      mVar <- MVar.in[G, F, A](initial)
      mutex <- Sync[G].delay(new Mutex(mVar, ref))
    } yield mutex
  }
}
