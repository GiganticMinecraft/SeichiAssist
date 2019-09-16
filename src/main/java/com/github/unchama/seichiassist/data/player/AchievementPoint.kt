package com.github.unchama.seichiassist.data.player

data class AchievementPoint(val fromUnlockedAchievements: Int = 0, val used: Int = 0, val conversionCount: Int = 0) {
  val cumulativeTotal = fromUnlockedAchievements + conversionCount * 3

  val left = cumulativeTotal - used
}