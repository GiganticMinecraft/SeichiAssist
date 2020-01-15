package com.github.unchama.concurrent

import cats.effect.IO
import com.github.unchama.util.effect.IOUtils._

import scala.concurrent.duration.FiniteDuration

abstract class RepeatingTask {
  /**
   * [[getRepeatInterval]]で指定される長さの待機処理と[[runRoutine]]を交互に行っていくプログラム.
   *
   * [[runRoutine]]が例外を吐こうと実行が終了することはない.
   */
  lazy val launch: IO[Nothing] = {
    val sleep: IO[Unit] = for {
      interval <- getRepeatInterval
      _ <- IO.timer(context).sleep(interval)
    } yield ()

    val fireRoutine: IO[Unit] = {
      for {
        _ <- IO.shift(context)
        _ <- runRoutine
      } yield ()
    }.runAsync {
      case Left(error) => IO {
        println("定期実行タスクの実行中にエラーが発生しました。")
        error.printStackTrace()
      }
      case Right(value) => IO.pure(value)
    }.toIO

    forever {
      for {
        _ <- sleep
        _ <- fireRoutine
      } yield ()
    }
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
}
