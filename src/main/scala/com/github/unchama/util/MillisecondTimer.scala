package com.github.unchama.util

import cats.effect.IO

class MillisecondTimer private() {
  private var startTime: Long = 0

  def resetTimer(): Unit = {
    startTime = System.nanoTime()
  }

  def sendLapTimeMessage(message: String): Unit = {
    val recordedNanoSecondDuration = System.nanoTime() - startTime

    println(s"$message(time: ${recordedNanoSecondDuration / 1000000L} ms)")

    startTime = System.nanoTime()
  }
}

object MillisecondTimer {
  def getInitializedTimerInstance: MillisecondTimer = {
    val timer = new MillisecondTimer()
    timer.resetTimer()
    timer
  }

  def time[R](program: => R)(message: String): R = {
    val t = getInitializedTimerInstance
    val result = program
    t.sendLapTimeMessage(message)
    result
  }

  def timeIO[R](program: IO[R])(message: String): IO[R] =
    for {
      t <- IO { getInitializedTimerInstance }
      result <- program
      _ <- IO { t.sendLapTimeMessage(message) }
    } yield result
}