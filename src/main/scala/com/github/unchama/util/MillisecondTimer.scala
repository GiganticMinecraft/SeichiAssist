package com.github.unchama.util

class MillisecondTimer private() {
  private var startTime: Long = 0

  def resetTimer(): Unit = {
    startTime = System.nanoTime()
  }

  def sendLapTimeMessage(message: String): Unit = {
    val recordedNanoSecondDuration = System.nanoTime() - startTime

    println(s"$message(time: ${recordedNanoSecondDuration / 1000L} ms)")

    startTime = System.nanoTime()
  }
}

object MillisecondTimer {
  def getInitializedTimerInstance: MillisecondTimer = {
    val timer = new MillisecondTimer()
    timer.resetTimer()
    timer
  }
}