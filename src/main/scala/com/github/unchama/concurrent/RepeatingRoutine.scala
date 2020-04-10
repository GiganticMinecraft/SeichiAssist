package com.github.unchama.concurrent

import cats.Monad
import cats.effect.IO

import scala.concurrent.duration.FiniteDuration

trait RepeatingRoutine {
  import cats.implicits._

  /**
   * [[getRepeatInterval]]で指定される長さの待機処理と、
   * [[routineAction]]を交互に行っていくプログラム。
   *
   * [[routineAction]]が例外を吐いて終了するか`false`を返すと実行が終了する。
   */
  lazy val launch: IO[Unit] = {
    val recoveringRoutine: IO[Boolean] =
      routineAction.redeemWith(
        error => IO {
          println("ルーチンの実行中にエラーが発生しました。")
          error.printStackTrace()

          false
        },
        IO.pure
      )

    Monad[IO]
      .iterateWhile(sleepBetweenRoutines >> recoveringRoutine)(identity)
      .as(())
  }

  /**
   * [[routineAction]]の実行、及びスリープ処理に使用される[[RepeatingTaskContext]].
   *
   * ここにサーバーメインスレッドでの実行コンテキストは渡してはならず、
   * [[routineAction]]の実行がサーバーメインスレッドで行われてほしければ、
   * [[routineAction]]内でコンテキストをシフトすべきである。
   */
  val context: RepeatingTaskContext

  /**
   * 次回のルーチン実行までどのくらい間隔を開けるかを計算する
   */
  val getRepeatInterval: IO[FiniteDuration]

  val routineAction: IO[Boolean]

  val sleepBetweenRoutines: IO[Unit] =
    getRepeatInterval >>= (IO.timer(context).sleep(_))
}
