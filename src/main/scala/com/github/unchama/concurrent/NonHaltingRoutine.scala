package com.github.unchama.concurrent

import cats.Monad
import cats.effect.IO

import scala.concurrent.duration.FiniteDuration

abstract class NonHaltingRoutine {
  import cats.implicits._

  /**
   * [[getRepeatInterval]]で指定される長さの待機処理と[[runRoutine]]を交互に行っていくプログラム.
   *
   * [[runRoutine]]が例外を吐こうと実行が終了することはない.
   */
  lazy val launch: IO[Nothing] = {
    val recoveringRoutine: IO[Unit] = {
      runRoutine.redeemWith(
        error => IO {
          println("定期実行タスクの実行中にエラーが発生しました。")
          error.printStackTrace()
        },
        _ => IO.unit
      )
    }

    Monad[IO].foreverM(sleepBetweenRoutines >> recoveringRoutine)
  }

  /**
   * [[runRoutine]]の実行、及びスリープ処理に使用される[[RepeatingTaskContext]].
   *
   * サーバーメインスレッドでの実行コンテキストは渡してはならず、
   * [[runRoutine]]の実行がサーバーメインスレッドで行われてほしければ、
   * [[runRoutine]]内でコンテキストをシフトすべきである。
   */
  val context: RepeatingTaskContext

  protected val getRepeatInterval: IO[FiniteDuration]
  protected val runRoutine: IO[Any]

  val sleepBetweenRoutines: IO[Unit] =
    getRepeatInterval >>= (IO.timer(context).sleep(_))
}
