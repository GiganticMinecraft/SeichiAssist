package com.github.unchama.seichiassist.task

import kotlinx.coroutines.delay

abstract class RepeatedTaskLauncher {
  suspend fun launch(): Nothing {
    while (true) {
      delay(getRepeatIntervalTicks() * 50)
      runRoutine()
    }
  }

  protected abstract fun getRepeatIntervalTicks(): Long

  protected abstract suspend fun runRoutine()
}