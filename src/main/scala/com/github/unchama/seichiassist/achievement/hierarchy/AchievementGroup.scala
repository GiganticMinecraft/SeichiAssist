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

  val entryIdToGroup: Map[Int, AchievementGroup] = 
    (1001 to 1012).map(id => id -> BrokenBlockRanking).toMap ++
    (2001 to 2014).map(id => id -> PlacedBlockAmount).toMap ++
    (3001 to 3019).map(id => id -> BrokenBlockAmount).toMap ++
    (4001 to 4023).map(id => id -> PlayTime).toMap ++
    (5101 to 5125).map(id => id -> TotalLogins).toMap ++
    (5001 to 5008).map(id => id -> ConsecutiveLogins).toMap ++
    (6001 to 6008).map(id => id -> VoteCounts).toMap ++
    (7001 to 7027).map(id => id -> OfficialEvent).toMap ++
    (7901 to 7906).map(id => id -> OfficialEvent).toMap ++
    (8001 to 8003).map(id => id -> Secrets).toMap ++
    (9001 to 9047).map(id => id -> Anniversaries).toMap

  def getGroupNameByEntryId(entryId: Int): Option[String] = {
    entryIdToGroup.get(entryId) match {
      case Some(group) => Some(group.name)
      case None => None
    }
  }
}
