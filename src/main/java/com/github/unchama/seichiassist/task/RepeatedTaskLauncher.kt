package com.github.unchama.seichiassist.task

import kotlinx.coroutines.delay

abstract class RepeatedTaskLauncher {
  suspend fun launch(): Nothing {
    while (true) {
      delay(getRepeatIntervalTicks() * 50)
      try {
        runRoutine()
      } catch (e: Exception) {
        println("Caught an exception while executing repeating task.")
        e.printStackTrace()
      }
    }
  }

  protected abstract fun getRepeatIntervalTicks(): Long

  protected abstract suspend fun runRoutine()
}