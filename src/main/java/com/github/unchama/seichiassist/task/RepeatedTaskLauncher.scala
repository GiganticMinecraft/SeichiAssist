package com.github.unchama.seichiassist.task

abstract class RepeatedTaskLauncher {
  suspend def launch(): Nothing {
    while (true) {
      delay(getRepeatIntervalTicks() * 50)
      runRoutine()
    }
  }

  protected abstract def getRepeatIntervalTicks(): Long

  protected abstract suspend def runRoutine()
}