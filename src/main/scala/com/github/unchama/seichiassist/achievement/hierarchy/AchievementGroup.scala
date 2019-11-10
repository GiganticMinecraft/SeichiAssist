package com.github.unchama.seichiassist.achievement.hierarchy

import com.github.unchama.seichiassist.achievement.{AchievementId, SeichiAchievement}
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._

sealed abstract class AchievementGroup[+Parent <: Singleton](val name: String,
                                                             achievementIds: Seq[AchievementId],
                                                             val parent: Parent) {
  val achievements: Seq[SeichiAchievement] =
    SeichiAchievement.values.filter(achievementIds.contains)
}

object AchievementGroup {
  case object BrokenBlockAmount
    extends AchievementGroup("整地量", 3001 to 3019, BrokenBlock)

  case object BrokenBlockRanking
    extends AchievementGroup("整地神ランキング", 1001 to 1012, BrokenBlock)


  case object PlayTime
    extends AchievementGroup("参加時間", 4001 to 4023, Login)

  case object TotalLogins
    extends AchievementGroup("通算ログイン", 5001 to 5008, Login)

  case object ConsecutiveLogins
    extends AchievementGroup("連続ログイン", 5101 to 5120, Login)

  case object Anniversaries
    extends AchievementGroup("記念日", 9001 to 9036, Login)


  case object MebiusBreeder
    extends AchievementGroup("MEBIUSブリーダー", Seq.empty, Challenges)

  case object StarLevel
    extends AchievementGroup("スターレベル", Seq.empty, Challenges)


  case object OfficialEvent
    extends AchievementGroup("公式イベント", (7001 to 7027) ++ (7901 to 7906), Specials)

  case object VoteCounts
    extends AchievementGroup("JMS投票数", 6001 to 6008, Specials)

  case object Secrets
    extends AchievementGroup("極秘任務", 8001 to 8003, Specials)
}
