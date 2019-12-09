package com.github.unchama.seichiassist.achievement.hierarchy

import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._

sealed abstract class AchievementGroup(val name: String, val parent: AchievementCategory)

object AchievementGroup {
  case object BrokenBlockAmount
    extends AchievementGroup("整地量", BrokenBlock)

  case object BrokenBlockRanking
    extends AchievementGroup("整地神ランキング", BrokenBlock)

  case object PlacedBlockAmount
    extends AchievementGroup("建築量", Building)


  case object PlayTime
    extends AchievementGroup("参加時間", Login)

  case object TotalLogins
    extends AchievementGroup("通算ログイン", Login)

  case object ConsecutiveLogins
    extends AchievementGroup("連続ログイン", Login)

  case object Anniversaries
    extends AchievementGroup("記念日", Login)


  case object MebiusBreeder
    extends AchievementGroup("MEBIUSブリーダー", Challenges)

  case object StarLevel
    extends AchievementGroup("スターレベル", Challenges)


  case object OfficialEvent
    extends AchievementGroup("公式イベント", Specials)

  case object VoteCounts
    extends AchievementGroup("JMS投票数", Specials)

  case object Secrets
    extends AchievementGroup("極秘任務", Specials)
}
