package com.github.unchama.seichiassist.data.player
case class StarLevel(val fromBreakAmount: Int = 0, val fromConnectionTime: Int = 0, val fromEventAchievement: Int = 0) {
  def total() = fromBreakAmount + fromConnectionTime + fromEventAchievement
}