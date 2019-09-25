package com.github.unchama.seichiassist.data.player

case class AchievementPoint(fromUnlockedAchievements: Int = 0, used: Int = 0, conversionCount: Int = 0) {
  val cumulativeTotal: Int = fromUnlockedAchievements + conversionCount * 3

  val left: Int = cumulativeTotal - used
}