package com.github.unchama.concurrent

import cats.Monad
import cats.effect.IO

trait NonHaltingRoutine extends RepeatingRoutine {
  import cats.implicits._

  /**
   * この実装では、スリープと[[routineAction]]は交互に実行されるが、
   * [[routineAction]]が例外を吐こうとfalseを返そうと実行が終了することはない.
   */
  override lazy val launch: IO[Nothing] = {
    val recoveringRoutine: IO[Unit] = {
      routineAction.redeemWith(
        error => IO {
          println("定期実行タスクの実行中にエラーが発生しました。")
          error.printStackTrace()
        },
        _ => IO.unit
      )
    }

    Monad[IO].foreverM(sleepBetweenRoutines >> recoveringRoutine)
  }
}
