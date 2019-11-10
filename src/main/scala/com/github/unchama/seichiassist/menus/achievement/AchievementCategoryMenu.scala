package com.github.unchama.seichiassist.menus.achievement

import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup._
import com.github.unchama.seichiassist.achievement.hierarchy.{AchievementCategory, AchievementGroup}
import org.bukkit.Material

object AchievementCategoryMenu {
  type AchievementGroupRepr[Parent] = (AchievementGroup[Parent], Material)

  def groupsLayoutFor(achievementCategory: AchievementCategory): Map[Int, AchievementGroupRepr[AchievementCategory]] =
    achievementCategory match {
      case BrokenBlock =>
        Map(
          9 * 1 + 3 -> (BrokenBlockAmount, Material.IRON_PICKAXE),
          9 * 1 + 5 -> (BrokenBlockRanking, Material.DIAMOND_PICKAXE)
        )
      case Building =>
        Map()
      case Login =>
        Map(
          9 * 1 + 1 -> (PlayTime, Material.COMPASS),
          9 * 1 + 3 -> (TotalLogins, Material.BOOK),
          9 * 1 + 5 -> (ConsecutiveLogins, Material.BOOK_AND_QUILL),
          9 * 1 + 7 -> (Anniversaries, Material.NETHER_STAR)
        )
      case Challenges =>
        Map(
          9 * 1 + 3 -> (MebiusBreeder, Material.DIAMOND_HELMET),
          9 * 1 + 5 -> (StarLevel, Material.GOLD_INGOT)
        )
      case Specials =>
        Map(
          9 * 1 + 2 -> (OfficialEvent, Material.COMPASS),
          9 * 1 + 4 -> (VoteCounts, Material.BOOK),
          9 * 1 + 6 -> (Secrets, Material.DIAMOND_BARDING),
        )
    }
}
