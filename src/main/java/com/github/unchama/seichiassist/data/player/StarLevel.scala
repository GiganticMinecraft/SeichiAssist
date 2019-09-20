package com.github.unchama.seichiassist.data.player

case class StarLevel(fromBreakAmount: Int = 0, fromConnectionTime: Int = 0, fromEventAchievement: Int = 0) {

  def total(): Int = fromBreakAmount + fromConnectionTime + fromEventAchievement

}