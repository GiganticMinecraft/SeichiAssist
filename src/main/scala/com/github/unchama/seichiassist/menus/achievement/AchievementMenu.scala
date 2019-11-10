package com.github.unchama.seichiassist.menus.achievement

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.CommonSoundEffects
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._
import com.github.unchama.seichiassist.menus.{ColorScheme, CommonButtons}
import com.github.unchama.targetedeffect.sequentialEffect
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player

object AchievementMenu extends Menu {
  import com.github.unchama.menuinventory.InventoryRowSize._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

  override val frame: MenuFrame = MenuFrame(4.rows(), "実績・二つ名システム")

  type AchievementCategoryRepr = (AchievementCategory, Material)

  val categoryLayout: Map[Int, AchievementCategoryRepr] =
    Map(
      9 * 1 + 1 -> (BrokenBlock, Material.GOLD_PICKAXE),
      9 * 1 + 3 -> (Building, Material.GLASS),
      9 * 1 + 5 -> (Login, Material.COMPASS),
      9 * 1 + 7 -> (Challenges, Material.BLAZE_POWDER),
      9 * 2 + 4 -> (Specials, Material.EYE_OF_ENDER)
    )

  val menuLayout: Map[Int, Button] = {
    def buttonFor(categoryRepr: AchievementCategoryRepr): Button =
      categoryRepr match {
        case (category, material) =>
          val includedGroups =
            AchievementCategoryMenu.groupsLayoutFor(category).values.map(_._1)

          val partialBuilder =
            new IconItemStackBuilder(material).lore(ColorScheme.navigation(s"「${category.name}」"))

          if (includedGroups.nonEmpty) {
            Button(
              partialBuilder
                .lore(List(s"${RED}以下の実績が含まれます。") ++ includedGroups.map(s"$AQUA「" + _.name + "」"))
                .build(),
              action.LeftClickButtonEffect(
                sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  AchievementCategoryMenu(category).open
                )
              )
            )
          } else {
            Button(
              partialBuilder.lore(s"${YELLOW}今後実装予定のカテゴリです。").build(),
              action.LeftClickButtonEffect(com.github.unchama.targetedeffect.emptyEffect)
            )
          }
      }

    val categoryButtonsSection = categoryLayout.view.mapValues(buttonFor).toMap

    val toggleTitleToPlayerLevelButton: Button = ???
    val toTitleConfigurationMenu: Button = ???

    categoryButtonsSection ++
      Map(
        0 -> toggleTitleToPlayerLevelButton,
        8 -> toTitleConfigurationMenu,
        9 * 3 -> CommonButtons.openStickMenu
      )
  }

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = IO.pure(MenuSlotLayout(menuLayout))
}
