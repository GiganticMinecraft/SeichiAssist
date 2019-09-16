package com.github.unchama.seichiassist.effect.breaking

import org.bukkit.scheduler.BukkitRunnable

abstract class RoundedTask : BukkitRunnable() {
  private var round = 0

  abstract fun firstAction()

  abstract fun secondAction()

  fun otherwiseAction() {
    cancel()
  }

  override fun run() {
    round++
    when (round) {
      1 -> firstAction()
      2 -> secondAction()
      else -> otherwiseAction()
    }
  }
}
