package com.github.unchama.seichiassist.data.playerdata

data class StarLevel(val fromBreakAmount: Int = 0, val fromConnectionTime: Int = 0, val fromEventAchievement: Int = 0) {
  fun sum() = fromBreakAmount + fromConnectionTime + fromEventAchievement
}