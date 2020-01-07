package com.github.unchama.seichiassist.activeskill.effect.breaking

import org.bukkit.scheduler.BukkitRunnable

abstract class RoundedTask extends BukkitRunnable() {
  private var round = 0

  def firstAction(): Unit

  def secondAction(): Unit

  override def run(): Unit = {
    round += 1
    round match {
      case 1 => firstAction()
      case 2 => secondAction()
      case _ => otherwiseAction()
    }
  }

  def otherwiseAction(): Unit = {
    cancel()
  }
}
