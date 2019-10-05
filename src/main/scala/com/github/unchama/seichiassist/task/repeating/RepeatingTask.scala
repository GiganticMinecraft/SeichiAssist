package com.github.unchama.seichiassist.task.repeating

import java.util.concurrent.TimeUnit

import cats.effect.{IO, Timer}
import com.github.unchama.util.effect.IOUtils._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

abstract class RepeatingTask {
  protected val getRepeatIntervalTicks: IO[Long]

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
   * [[getRepeatIntervalTicks]]で指定される長さの待機処理と[[runRoutine]]を交互に行っていくプログラム.
   *
   * [[runRoutine]]が例外を吐こうと実行が終了することはない.
   */
  lazy val launch: IO[Nothing] = {
    val sleep = for {
      intervalTicks <- getRepeatIntervalTicks
      interval = FiniteDuration(intervalTicks * 50, TimeUnit.MILLISECONDS)
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
      _ <- IO.shift(taskExecutionContext)
      loop <- forever {
        for {
          _ <- sleep
          _ <- routineExecution
        } yield ()
      }
    } yield loop
  }
}