package com.github.unchama.seichiassist.effect.breaking

import org.bukkit.scheduler.BukkitRunnable
abstract class RoundedTask  extends  BukkitRunnable() {
  private var round = 0

  abstract def firstAction()

  abstract def secondAction()

  def otherwiseAction() {
    cancel()
  }

  override def run() {
    round += 1
      round match {
      case 1 => firstAction()
      case 2 => secondAction()
      case _ => otherwiseAction()
    }
  }
}
