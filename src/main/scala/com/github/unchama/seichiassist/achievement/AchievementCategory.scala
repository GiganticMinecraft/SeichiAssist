package com.github.unchama.seichiassist.achievement

import org.bukkit.Material

case class AchievementGroup(name: String, representation: Material, achievements: Seq[SeichiAchievement])

object AchievementGroup {
  def apply(name: String, representation: Material, achievementIds: Seq[Int]) =
    AchievementGroup(name, representation, SeichiAchievement.values.filter(achievementIds.contains))
}

case class AchievementCategory(name: String, representation: Material, groups: Seq[AchievementGroup])

object AchievementGroups {
  val brokenBlockAmount =
    AchievementGroup("整地量", Material.IRON_PICKAXE, 3001 to 3019)

  val brokenBlockRanking =
    AchievementGroup("整地神ランキング", Material.DIAMOND_PICKAXE, 1001 to 1012)


  val playTime =
    AchievementGroup("参加時間", Material.COMPASS, 4001 to 4023)

  val totalLogins =
    AchievementGroup("通算ログイン", Material.BOOK, 4001 to 4023)

  val consecutiveLogins =
    AchievementGroup("連続ログイン", Material.BOOK_AND_QUILL, 4001 to 4023)

  val anniversaries =
    AchievementGroup("記念日", Material.NETHER_STAR, 9001 to 9036)


  val mebiusBreeder =
    AchievementGroup("MEBIUSブリーダー", Material.DIAMOND_HELMET, Seq.empty[SeichiAchievement])

  val starLevel =
    AchievementGroup("スターレベル", Material.GOLD_INGOT, Seq.empty[SeichiAchievement])


  val officialEvent =
    AchievementGroup("公式イベント", Material.BLAZE_POWDER, (7001 to 7027) ++ (7901 to 7906))

  val voteCounts =
    AchievementGroup("JMS投票数", Material.YELLOW_FLOWER, 6001 to 6008)

  val secrets =
    AchievementGroup("極秘任務", Material.DIAMOND_BARDING, 8001 to 8003)
}

object AchievementCategories {
  import AchievementGroups._

  val brokenBlock =
    AchievementCategory(
      "整地", Material.GOLD_PICKAXE,
      Seq(
        brokenBlockAmount, brokenBlockRanking
      )
    )

  val building =
    AchievementCategory(
      "建築", Material.GLASS,
      Seq()
    )

  val login =
    AchievementCategory(
      "ログイン", Material.COMPASS,
      Seq(
        playTime, totalLogins, consecutiveLogins, anniversaries
      )
    )

  val challenges =
    AchievementCategory(
      "やりこみ", Material.BLAZE_POWDER,
      Seq(
        mebiusBreeder, starLevel
      )
    )

  val specials =
    AchievementCategory(
      "特殊", Material.EYE_OF_ENDER,
      Seq(
        officialEvent, voteCounts, secrets
      )
    )
}
