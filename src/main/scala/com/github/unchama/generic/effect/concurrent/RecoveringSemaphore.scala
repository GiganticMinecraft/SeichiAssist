package com.github.unchama.generic.effect.concurrent

import cats.effect.syntax.all._
import cats.effect.{Async, Sync, Temporal}

import scala.concurrent.duration.FiniteDuration
import cats.effect.std.Semaphore

/**
 * 一定時間をおいて利用可能になるようなセマフォ。
 */
final class RecoveringSemaphore[F[_]: Temporal] private (semaphore: Semaphore[F]) {

  import cats.implicits._

  /**
   * このセマフォが利用可能であれば `action` を実行し、 `recoverTime` の間使用不能にする。
   */
  def tryUse[U](action: F[U], default: => F[U])(recoverTime: FiniteDuration): F[U] = {
    // セマフォを`recoverTime` の間使用不能にし、その後解放する作用
    val releaseProgram: F[Unit] = Temporal[F].sleep(recoverTime) >> semaphore.release

    semaphore.tryAcquire.flatMap { acquired =>
      if (acquired)
        action.guarantee(releaseProgram.start.void)
      else
        default
    }
  }
}

object RecoveringSemaphore {

  import cats.implicits._

  def newIn[G[_]: Sync, F[_]: Async]: G[RecoveringSemaphore[F]] =
    Semaphore.in[G, F](1).map(new RecoveringSemaphore(_))

}
