package com.github.unchama.seichiassist.data.player

data class StarLevel(val fromBreakAmount: Int = 0, val fromConnectionTime: Int = 0, val fromEventAchievement: Int = 0) {
  fun total() = fromBreakAmount + fromConnectionTime + fromEventAchievement
}