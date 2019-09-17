package com.github.unchama.util

class MillisecondTimer private constructor() {
  private var startTime: Long = 0

  fun resetTimer() {
    startTime = System.nanoTime()
  }

  fun sendLapTimeMessage(message: String) {
    val recordedNanoSecondDuration = System.nanoTime() - startTime

    println("$message(time: ${recordedNanoSecondDuration / 1000L} ms)")

    startTime = System.nanoTime()
  }

  companion object {
    fun getInitializedTimerInstance(): MillisecondTimer = MillisecondTimer().apply { resetTimer() }
  }
}
