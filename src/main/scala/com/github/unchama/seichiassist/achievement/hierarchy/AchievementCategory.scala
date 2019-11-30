package com.github.unchama.seichiassist.achievement.hierarchy

sealed abstract class AchievementCategory(val name: String)

object AchievementCategory {
  case object BrokenBlock extends AchievementCategory("整地")
  case object Building extends AchievementCategory("建築")
  case object Login extends AchievementCategory("ログイン")
  case object Challenges extends AchievementCategory("やりこみ")
  case object Specials extends AchievementCategory("特殊")
}
