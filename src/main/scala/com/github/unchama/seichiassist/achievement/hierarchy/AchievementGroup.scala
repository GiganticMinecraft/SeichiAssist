package com.github.unchama.seichiassist.achievement.hierarchy

import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._

sealed abstract class AchievementGroup(val name: String, val parent: AchievementCategory)

object AchievementGroup {
  case object BrokenBlockAmount extends AchievementGroup("整地量", BrokenBlock)

  case object BrokenBlockRanking extends AchievementGroup("整地神ランキング", BrokenBlock)

  case object PlacedBlockAmount extends AchievementGroup("建築量", Building)

  case object PlayTime extends AchievementGroup("参加時間", Login)

  case object TotalLogins extends AchievementGroup("通算ログイン", Login)

  case object ConsecutiveLogins extends AchievementGroup("連続ログイン", Login)

  case object Anniversaries extends AchievementGroup("記念日", Login)

  case object MebiusBreeder extends AchievementGroup("MEBIUSブリーダー", Challenges)

  case object StarLevel extends AchievementGroup("スターレベル", Challenges)

  case object OfficialEvent extends AchievementGroup("公式イベント", Specials)

  case object VoteCounts extends AchievementGroup("JMS投票数", Specials)

  case object Secrets extends AchievementGroup("極秘任務", Specials)

  private val achievementIdRangeToGroupNameList = List(
    (1001 to 1012, BrokenBlockRanking),
    (2001 to 2014, PlacedBlockAmount),
    (3001 to 3019, BrokenBlockAmount),
    (4001 to 4023, PlayTime),
    (5101 to 5125, TotalLogins),
    (5001 to 5008, ConsecutiveLogins),
    (6001 to 6008, VoteCounts),
    (7001 to 7027, OfficialEvent),
    (7901 to 7906, OfficialEvent),
    (8001 to 8003, Secrets),
    (9001 to 9047, Anniversaries)
  )

  /**
   * @return 実績IDから実績IDが属する実績グループ名を取得する
   */
  def getGroupNameByEntryId(entryId: Int): Option[String] = {
    achievementIdRangeToGroupNameList
    .collectFirst {
      case (range, group) if range contains entryId => group.name
    }
  }
}
