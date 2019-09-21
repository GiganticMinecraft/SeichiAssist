package com.github.unchama.seichiassist.task

import com.github.unchama.util.kotlin2scala.SuspendingMethod

abstract class RepeatedTaskLauncher {
  @SuspendingMethod def launch(): Nothing = {
    while (true) {
      delay(getRepeatIntervalTicks() * 50)
      runRoutine()
    }
  }

  protected abstract def getRepeatIntervalTicks(): Long

  protected abstract @SuspendingMethod def runRoutine()
}