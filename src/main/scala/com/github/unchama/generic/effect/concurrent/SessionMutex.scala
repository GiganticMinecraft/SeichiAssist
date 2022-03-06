package com.github.unchama.generic.effect.concurrent

import cats.Monad
import cats.effect.{Concurrent, Sync}
import com.github.unchama.generic.ContextCoercion

class SessionMutex[F[_]: Monad, G[_]] private (underlying: Mutex[F, G, TryableFiber[F, Unit]]) {

  import cats.implicits._

  /**
   *   - 実行中のFiberがあれば停止し、Mutexの中身を停止したFiberで上書きしfalseを返す
   *   - そうでなければ、Mutexの中身を `startNewFiber` により提供されるFiberにて上書きしtrueを返す ような計算を返す。
   */
  def flipState(startNewFiber: F[TryableFiber[F, Unit]]): F[Boolean] =
    underlying.lockAndModify { fiber =>
      for {
        wasIncomplete <- fiber.cancelIfIncomplete
        newFiber <- if (wasIncomplete) TryableFiber.unit[F].pure[F] else startNewFiber
        // Fiberが完了していた <=> 新たなFiberをmutexへ入れた
      } yield (newFiber, !wasIncomplete)
    }

  /**
   *   - 実行中のFiberを持っていればそれを止めtrueを返し
   *   - そうでなければfalseを返す ようなIOを返す
   */
  def stopAnyFiber: F[Boolean] =
    underlying.lockAndModify { fiber =>
      for {
        wasIncomplete <- fiber.cancelIfIncomplete
      } yield (TryableFiber.unit[F], wasIncomplete)
    }
}

object SessionMutex {

  import cats.implicits._

  def newIn[F[_]: Concurrent, G[_]: Sync: ContextCoercion[*[_], F]]: G[SessionMutex[F, G]] =
    for {
      mutex <- Mutex.of[F, G, TryableFiber[F, Unit]](TryableFiber.unit[F])
    } yield new SessionMutex(mutex)

}
