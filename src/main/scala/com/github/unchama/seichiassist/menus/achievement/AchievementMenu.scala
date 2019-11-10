package com.github.unchama.seichiassist.menus.achievement

import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._
import org.bukkit.Material

object AchievementMenu {
  type AchievementCategoryRepr = (AchievementCategory, Material)

  val categoryLayout: Map[Int, AchievementCategoryRepr] =
    Map(
      9 * 1 + 1 -> (BrokenBlock, Material.GOLD_PICKAXE),
      9 * 1 + 3 -> (Building, Material.GLASS),
      9 * 1 + 5 -> (Login, Material.COMPASS),
      9 * 1 + 7 -> (Challenges, Material.BLAZE_POWDER),
      9 * 2 + 4 -> (Specials, Material.EYE_OF_ENDER)
    )
}
