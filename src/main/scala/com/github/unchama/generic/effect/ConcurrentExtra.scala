package com.github.unchama.generic.effect

import cats.Applicative
import cats.effect.concurrent.Deferred
import cats.effect.{CancelToken, Concurrent}

object ConcurrentExtra {
  import cats.implicits._

  /**
   * `f` を並行的に開始し、 `f` の計算をキャンセルする計算を`f`に渡してから結果をawaitする計算を返す。
   *
   * `f` 内で渡された `CancelToken[F]` を実行した際の動作は未定義となる。 実際、`f` 内のキャンセルはそれ自身の終了をブロックしながらawaitするため、
   * ハングすることが予想される。
   */
  def withSelfCancellation[F[_]: Concurrent, A](f: CancelToken[F] => F[A]): F[A] =
    for {
      //  [start `awaitToken >>= f`]--
      //             |               |
      //      [fill promise]         |
      //             |--->[await promise completion]
      //             |               |
      //             |        [`f` yielding a]
      //             |               |
      //     [await completion]<------
      //             |
      // yield a <----

      tokenPromise <- Deferred[F, CancelToken[F]]

      fiber <- Concurrent[F].start(tokenPromise.get.flatMap(f))

      _ <- tokenPromise.complete(fiber.cancel)

      a <- fiber.join
    } yield a

  import cats.effect._

  /**
   * 与えられた複数の入力プログラムをすべて並列に実行するようなプログラムを構築します。
   * 構築されたプログラムは次のような挙動をします：
   *  - 構築されたプログラムの実行 fiber がキャンセルされた時、
   *    その時点で並列で走っている入力プログラムの実行 fiber がすべてキャンセルされます
   *  - 入力プログラムの実行 fiber がエラー等で早期終了しても、他の実行 fiber への影響はありません
   *    （一つの fiber が異常終了しても、他の fiber のキャンセルなどは行われません）
   *  - 結果の `List[Either[Throwable, A]]` は入力プログラムの終了結果を
   *    (入力プログラムが与えられた順と同じ順で) 保持しています。
   *
   *    各値は
   *    - `Right[A]` だった場合、入力プログラムが `A` を結果として正常終了したこと
   *    - `Left[Throwable]` だった場合、入力プログラムが例外を送出して異常終了したこと
   *    をそれぞれ表します。
   */
  def attemptInParallel[F[_]: ConcurrentEffect, A](
    programs: List[F[A]]
  ): F[List[Either[Throwable, A]]] = {
    ConcurrentEffect[F]
      .bracketCase(programs.traverse(Concurrent[F].start(_)))(_.traverse(_.join.attempt)) {
        case (fibers, ExitCase.Canceled) => fibers.traverse(_.cancel).void
        case _                           => Applicative[F].unit
      }
  }
}
