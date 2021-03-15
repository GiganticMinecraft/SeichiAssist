package com.github.unchama.generic.effect.concurrent

import cats.Monad
import cats.effect.concurrent.Semaphore
import cats.effect.{Bracket, Concurrent, Sync, Timer}

import scala.concurrent.duration.FiniteDuration

/**
 * 一定時間をおいて利用可能になるようなセマフォ。
 */
final class RecoveringSemaphore[F[_] : Timer : Bracket[*[_], Throwable]] private(semaphore: Semaphore[F]) {

  import cats.implicits._

  /**
   * このセマフォが利用可能であれば `action` を実行し、 `recoverTime` の間使用不能にする。
   */
  def tryUse[U](action: F[U])(recoverTime: FiniteDuration): F[Unit] =
    semaphore.tryAcquire.flatMap { acquired =>
      if (acquired)
        Bracket[F, Throwable].guarantee(action.as(())) {
          Timer[F].sleep(recoverTime) >> semaphore.release
        }
      else
        Monad[F].unit
    }

}

object RecoveringSemaphore {

  import cats.implicits._

  def newIn[G[_] : Sync, F[_] : Concurrent : Timer]: G[RecoveringSemaphore[F]] =
    Semaphore.in[G, F](1).map(new RecoveringSemaphore(_))

}
