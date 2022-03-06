package com.github.unchama.generic.effect.concurrent

import cats.Monad
import cats.effect.concurrent.Semaphore
import cats.effect.{Bracket, Concurrent, Sync, Timer}

import scala.concurrent.duration.FiniteDuration

/**
 * 一定時間をおいて利用可能になるようなセマフォ。
 */
final class RecoveringSemaphore[F[_]: Timer: Concurrent] private (semaphore: Semaphore[F]) {

  import cats.effect.implicits._
  import cats.implicits._

  /**
   * このセマフォが利用可能であれば `action` を実行し、 `recoverTime` の間使用不能にする。
   */
  def tryUse[U](action: F[U])(recoverTime: FiniteDuration): F[Unit] = {
    // セマフォを`recoverTime` の間使用不能にし、その後解放する作用
    val releaseProgram: F[Unit] = Timer[F].sleep(recoverTime) >> semaphore.release

    semaphore.tryAcquire.flatMap { acquired =>
      if (acquired)
        Bracket[F, Throwable]
          .guarantee(action) {
            releaseProgram.start.as(())
          }
          .as(())
      else
        Monad[F].unit
    }
  }
}

object RecoveringSemaphore {

  import cats.implicits._

  def newIn[G[_]: Sync, F[_]: Concurrent: Timer]: G[RecoveringSemaphore[F]] =
    Semaphore.in[G, F](1).map(new RecoveringSemaphore(_))

}
