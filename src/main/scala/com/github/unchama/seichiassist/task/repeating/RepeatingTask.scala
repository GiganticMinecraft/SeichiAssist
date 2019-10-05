package com.github.unchama.seichiassist.task.repeating

import cats.effect.{IO, Timer}
import com.github.unchama.util.effect.IOUtils._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

abstract class RepeatingTask {
  protected val getRepeatInterval: IO[FiniteDuration]

  protected val runRoutine: IO[Any]

  /**
   * [[runRoutine]]が実行される[[ExecutionContext]].
   *
   * [[runRoutine]]の中で更にコンテキストを切り替えても問題ないが,
   * 殆どの場合外部から実行コンテキストを指定するほうがわかりやすいため[[RepeatingTask]]に組み込んでいる.
   */
  val taskExecutionContext: ExecutionContext

  /**
   * 待機処理を担当する[[Timer]].
   */
  val sleepTimer: Timer[IO]

  /**
   * [[getRepeatInterval]]で指定される長さの待機処理と[[runRoutine]]を交互に行っていくプログラム.
   *
   * [[runRoutine]]が例外を吐こうと実行が終了することはない.
   */
  lazy val launch: IO[Nothing] = {
    val sleep = for {
      interval <- getRepeatInterval
      _ <- sleepTimer.sleep(interval)
    } yield ()

    val routineExecution = for {
      result <- runRoutine.attempt
      _ <- result match {
        case Left(error) => IO {
          println("Caught an exception while executing repeating task")
          error.printStackTrace()
        }
        case Right(value) => IO.pure(value)
      }
    } yield ()

    for {
      loop <- forever {
        for {
          _ <- sleep
          _ <- IO.shift(taskExecutionContext)
          _ <- routineExecution
        } yield ()
      }
    } yield loop
  }
}