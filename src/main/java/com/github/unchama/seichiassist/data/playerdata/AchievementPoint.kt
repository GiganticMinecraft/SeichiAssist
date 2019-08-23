package com.github.unchama.seichiassist.data.playerdata

data class AchievementPoint(val cumulativeTotal: Int = 0, val used: Int = 0, val conversionCount: Int = 0) {
  // いちいち変更するのは冗長
  val left = cumulativeTotal + conversionCount * 3 - used
}