package com.github.unchama.seichiassist.effect.breaking

abstract class RoundedTask  extends  BukkitRunnable() {
  private var round = 0

  abstract def firstAction()

  abstract def secondAction()

  def otherwiseAction() {
    cancel()
  }

  override def run() {
    round++
    when (round) {
      1 => firstAction()
      2 => secondAction()
      else => otherwiseAction()
    }
  }
}
