package com.github.unchama.generic.effect

import cats.effect.concurrent.Deferred
import cats.effect.{CancelToken, Concurrent}

object ConcurrentExtra {
  import cats.implicits._

  /**
   * `f` を並行的に開始し、 `f` の計算をキャンセルする計算を`f`に渡してから結果をawaitする計算を返す。
   *
   * `f` 内で渡された `CancelToken[F]` を実行した際の動作は未定義となる。
   * 実際、`f` 内のキャンセルはそれ自身の終了をブロックしながらawaitするため、
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
}
