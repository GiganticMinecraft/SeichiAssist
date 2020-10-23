package com.github.unchama.seichiassist.menus.achievement

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory
import com.github.unchama.seichiassist.achievement.hierarchy.AchievementCategory._
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.data.player.NicknameStyle
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.{ColorScheme, CommonButtons}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object AchievementMenu extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}
  import eu.timepit.refined.auto._

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}実績・二つ名システム")

  type AchievementCategoryRepr = (AchievementCategory, Material)

  val categoryLayout: Map[Int, AchievementCategoryRepr] =
    Map(
      ChestSlotRef(1, 1) -> (BrokenBlock, Material.GOLD_PICKAXE),
      ChestSlotRef(1, 3) -> (Building, Material.GLASS),
      ChestSlotRef(1, 5) -> (Login, Material.COMPASS),
      ChestSlotRef(1, 7) -> (Challenges, Material.BLAZE_POWDER),
      ChestSlotRef(2, 4) -> (Specials, Material.EYE_OF_ENDER)
    )

  val menuLayout: Map[Int, Button] = {
    def buttonFor(categoryRepr: AchievementCategoryRepr): Button =
      categoryRepr match {
        case (category, material) =>
          val includedGroups =
            AchievementCategoryMenu.groupsLayoutFor(category).values.map(_._1)

          val partialBuilder =
            new IconItemStackBuilder(material).title(ColorScheme.navigation(s"カテゴリ「${category.name}」"))

          if (includedGroups.nonEmpty) {
            Button(
              partialBuilder
                .lore(List(s"${RED}以下の実績が含まれます。") ++ includedGroups.map(s"$AQUA「" + _.name + "」"))
                .build(),
              action.LeftClickButtonEffect(
                SequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  AchievementCategoryMenu(category).open
                )
              )
            )
          } else {
            Button(
              partialBuilder.lore(s"${YELLOW}今後実装予定のカテゴリです。").build(),
              action.LeftClickButtonEffect(TargetedEffect.emptyEffect)
            )
          }
      }

    val categoryButtonsSection = categoryLayout.view.mapValues(buttonFor).toMap

    import com.github.unchama.targetedeffect._
    val toggleTitleToPlayerLevelButton = Button(
      new IconItemStackBuilder(Material.REDSTONE_TORCH_ON)
        .title(ColorScheme.navigation("整地Lvを表示"))
        .lore(List(
          s"${RED}このボタンをクリックすると、",
          s"$RED「整地Lv」に表示を切り替えます。",
          s"$YELLOW※反映されるまで最大1分ほどかかります。"
        ))
        .build(),
      action.LeftClickButtonEffect(
        FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
        TargetedEffect.delay { player =>
          SeichiAssist.playermap(player.getUniqueId).updateNickname(style = NicknameStyle.Level)
        }
      )
    )
    val toTitleConfigurationMenu: Button = Button(
      new IconItemStackBuilder(Material.ANVIL)
        .title(ColorScheme.navigation("「二つ名組み合わせシステム」"))
        .lore(s"${RED}設定画面を表示します。")
        .build(),
      action.LeftClickButtonEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        Kleisli.liftF(PluginExecutionContexts.syncShift.shift),
        TargetedEffect.delay { player =>
          player.openInventory(MenuInventoryData.computeRefreshedCombineMenu(player))
        }
      )
    )

    categoryButtonsSection ++
      Map(
        ChestSlotRef(0, 0) -> toggleTitleToPlayerLevelButton,
        ChestSlotRef(0, 8) -> toTitleConfigurationMenu,
        ChestSlotRef(3, 0) -> CommonButtons.openStickMenu
      )
  }

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = IO.pure(MenuSlotLayout(menuLayout))
}
