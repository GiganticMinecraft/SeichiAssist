package com.github.unchama.seichiassist.menus.achievement

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementGroup._
import com.github.unchama.seichiassist.achievement.hierarchy.{AchievementCategory, AchievementGroup}
import com.github.unchama.seichiassist.menus.achievement.group.AchievementGroupMenu
import com.github.unchama.seichiassist.menus.{ColorScheme, CommonButtons}
import com.github.unchama.seichiassist.{CommonSoundEffects, SkullOwners}
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player

object AchievementCategoryMenu {
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sync
  import eu.timepit.refined.auto._

  type AchievementGroupRepr = (AchievementGroup, Material)

  def groupsLayoutFor(achievementCategory: AchievementCategory): Map[Int, AchievementGroupRepr] =
    achievementCategory match {
      case BrokenBlock =>
        Map(
          ChestSlotRef(1, 3) -> (BrokenBlockAmount, Material.IRON_PICKAXE),
          ChestSlotRef(1, 5) -> (BrokenBlockRanking, Material.DIAMOND_PICKAXE)
        )
      case Building =>
        Map()
      case Login =>
        Map(
          ChestSlotRef(1, 1) -> (PlayTime, Material.COMPASS),
          ChestSlotRef(1, 3) -> (TotalLogins, Material.BOOK),
          ChestSlotRef(1, 5) -> (ConsecutiveLogins, Material.BOOK_AND_QUILL),
          ChestSlotRef(1, 7) -> (Anniversaries, Material.NETHER_STAR)
        )
      case Challenges =>
        Map(
          ChestSlotRef(1, 3) -> (MebiusBreeder, Material.DIAMOND_HELMET),
          ChestSlotRef(1, 5) -> (StarLevel, Material.GOLD_INGOT)
        )
      case Specials =>
        Map(
          ChestSlotRef(1, 2) -> (OfficialEvent, Material.BLAZE_POWDER),
          ChestSlotRef(1, 4) -> (VoteCounts, Material.YELLOW_FLOWER),
          ChestSlotRef(1, 6) -> (Secrets, Material.DIAMOND_BARDING),
        )
    }

  def buttonFor(achievementGroupRepr: AchievementGroupRepr): Button =
    achievementGroupRepr match {
      case (group, material) =>
        val partialBuilder =
          new IconItemStackBuilder(material)
            .title(ColorScheme.navigation(s"実績「${group.name}」"))

        import com.github.unchama.targetedeffect._

        if (AchievementGroupMenu.sequentialEntriesIn(group).nonEmpty) {
          Button(
            partialBuilder
              .lore(s"${RED}獲得状況を表示します。")
              .build(),
            LeftClickButtonEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              AchievementGroupMenu(group).open
            )
          )
        } else {
          Button(
            partialBuilder
              .lore(s"${RED}獲得状況を表示します。※未実装")
              .build(),
            LeftClickButtonEffect(emptyEffect)
          )
        }
    }

  def apply(category: AchievementCategory): Menu = {
    val groupButtons =
      groupsLayoutFor(category).view.mapValues(buttonFor).toMap

    val toMainMenuButton =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
        "実績・二つ名メニューへ",
        AchievementMenu,
      )

    new Menu {
      import com.github.unchama.menuinventory.syntax._

      override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}カテゴリ「${category.name}」")

      override def computeMenuLayout(player: Player): IO[MenuSlotLayout] =
        IO.pure(MenuSlotLayout(groupButtons ++ Map(ChestSlotRef(3, 0) -> toMainMenuButton)))
    }
  }
}
