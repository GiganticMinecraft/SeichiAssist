package com.github.unchama.util

import cats.effect.Sync
import org.slf4j.Logger

class MillisecondTimer private() {
  private var startTime: Long = 0

  def resetTimer(): Unit = {
    startTime = System.nanoTime()
  }

  /**
   * @deprecated use [[sendLapTimeMessageWithLogger]]
   */
  @Deprecated() def sendLapTimeMessage(message: String): Unit = {
    val recordedNanoSecondDuration = System.nanoTime() - startTime

    println(s"$message(time: ${recordedNanoSecondDuration / 1000000L} ms)")

    startTime = System.nanoTime()
  }

  def sendLapTimeMessageWithLogger(message: String)(implicit logger: Logger): Unit = {
    val recordedNanoSecondDuration = System.nanoTime() - startTime

    logger.info(s"$message(time: ${recordedNanoSecondDuration / 1000000L} ms)")

    startTime = System.nanoTime()
  }
}

object MillisecondTimer {
  def getInitializedTimerInstance: MillisecondTimer = {
    val timer = new MillisecondTimer()
    timer.resetTimer()
    timer
  }

  import cats.implicits._

  def timeF[F[_] : Sync, R](program: F[R])(message: String)(implicit logger: Logger): F[R] =
    for {
      timer <- Sync[F].delay {
        getInitializedTimerInstance
      }
      result <- program
      _ <- Sync[F].delay {
        timer.sendLapTimeMessageWithLogger(message)
      }
    } yield result
}
