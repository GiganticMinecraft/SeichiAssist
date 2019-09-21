package com.github.unchama.buildassist.menu

import com.github.unchama.buildassist.{BuildAssist, PlayerData}
import com.github.unchama.menuinventory.slot.button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.{Menu, MenuInventoryView}
import com.github.unchama.seichiassist.{CommonSoundEffects, Schedulers}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import com.github.unchama.{menuinventory, targetedeffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object BlockPlacementSkillMenu extends Menu {
  private object ConstantButtons {
    val buttonToOpenPreviousPage = run {
      val iconItemStack = IconItemStackBuilder(Material.BARRIER)
        .title(s"$YELLOW$UNDERLINE${BOLD}元のページへ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()

      button.Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          BuildMainMenu.open
        )
      )
    }
  }

  private case class ButtonComputations(val player: Player) extends AnyVal {
    @SuspendingMethod def computeButtonToToggleDirtPlacement() = recomputedButton {
      val playerData = BuildAssist.playermap[uniqueId]
      val currentStatus = playerData.zsSkillDirtFlag

      val iconItemStack = IconItemStackBuilder(Material.DIRT)
        .title(s"$YELLOW$UNDERLINE${BOLD}設置時に下の空洞を埋める機能")
        .lore(
          s"$RESET$AQUA${UNDERLINE}機能の使用設定： ${if (currentStatus) "ON" else "OFF"}",
          s"$RESET$AQUA${UNDERLINE}機能の範囲： 地下5マスまで"
        )
        .build()

      button.Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect { playerData.zsSkillDirtFlag = !currentStatus },
          s"${RED}土設置機能${if (currentStatus) "OFF" else "ON"}".asMessageEffect()
        )
      )
    }

    @SuspendingMethod def computeButtonToShowCurrentStatus() = recomputedButton {
      val playerData = BuildAssist.playermap[uniqueId]
      val isSkillEnabled = playerData.ZoneSetSkillFlag
      val skillRange = playerData.computeCurrentSkillRange()
      val isConsumingMineStack = playerData.zs_minestack_flag

      val iconItemStack = IconItemStackBuilder(Material.STONE)
        .title(s"$YELLOW$UNDERLINE${BOLD}現在の設定は以下の通りです")
        .lore(
          s"$RESET$AQUA${UNDERLINE}スキルの使用設定: ${if (isSkillEnabled) "ON" else "OFF"}",
          s"$RESET$AQUA${UNDERLINE}スキルの範囲設定: $skillRange×$skillRange",
          s"$RESET$AQUA${UNDERLINE}MineStack優先設定: ${if (isConsumingMineStack) "ON" else "OFF"}"
        )
        .build()

      button.Button(iconItemStack)
    }

    @SuspendingMethod def computeButtonToMaximizeRange() = run {
      val playerData = BuildAssist.playermap[uniqueId]
      val currentRange = playerData.computeCurrentSkillRange()

      val iconItemStack = SkullItemStackBuilder("MHF_ArrowUp")
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を最大値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： 11×11"
        )
        .amount(11)
        .build()

      button.Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect { playerData.AREAint = 5 },
          s"${RED}現在の範囲設定は 11×11 です".asMessageEffect(),
          open
        )
      )
    }

    @SuspendingMethod def computeButtonToIncreaseRange() = run {
      val playerData = BuildAssist.playermap[uniqueId]
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = currentRange + 2

      val iconItemStack = SkullItemStackBuilder("MHF_ArrowUp")
        .title(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階大きくする")
        .lore(
          List(
            s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange"
          ) +
      if (playerData.AREAint == 5) {
        List(
          s"$RESET${RED}これ以上範囲設定を大きくできません。"
        )
      } else {
        List(
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
          s"$RESET${RED}※範囲設定の最大値は11×11※"
        )
      }
      )
      .amount(7)
        .build()

      button.Button(
        iconItemStack,
        LeftClickButtonEffect(
          deferredEffect {
            if (playerData.AREAint < 5)
              sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                UnfocusedEffect { playerData.AREAint++ },
                s"${RED}現在の範囲設定は $changedRange×$changedRange です".asMessageEffect(),
                open
              )
            else EmptyEffect
          }
        )
      )
    }

    @SuspendingMethod def computeButtonToResetRange() = run {
      val playerData = BuildAssist.playermap[uniqueId]
      val currentRange = playerData.computeCurrentSkillRange()

      val iconItemStack = SkullItemStackBuilder("MHF_TNT")
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を初期値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： 5×5"
        )
        .amount(5)
        .build()

      button.Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect { playerData.AREAint = 2 },
          s"${RED}現在の範囲設定は 5×5 です".asMessageEffect(),
          open
        )
      )
    }

    @SuspendingMethod def computeButtonToDecreaseRange() = run {
      val playerData = BuildAssist.playermap[uniqueId]
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = currentRange + -2

      val iconItemStack = SkullItemStackBuilder("MHF_ArrowDown")
        .title(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階小さくする")
        .lore(
          List(
            s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange"
          ) +
      if (playerData.AREAint == 1) {
        List(
          s"${RED}これ以上範囲設定を小さくできません。"
        )
      } else {
        List(
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
          s"$RESET${RED}※範囲設定の最大値は3×3※"
        )
      }
      )
      .amount(3)
        .build()

      button.Button(
        iconItemStack,
        LeftClickButtonEffect(
          deferredEffect {
            if (playerData.AREAint > 1)
              sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                UnfocusedEffect { playerData.AREAint-- },
                s"${RED}現在の範囲設定は $changedRange×$changedRange です".asMessageEffect(),
                open
              )
            else EmptyEffect
          }
        )
      )
    }

    @SuspendingMethod def computeButtonToMinimizeRange() = run {
      val playerData = BuildAssist.playermap[uniqueId]
      val currentRange = playerData.computeCurrentSkillRange()

      val iconItemStack = SkullItemStackBuilder("MHF_ArrowDown")
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を最小値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： 3×3"
        )
        .amount(1)
        .build()

      button.Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect { playerData.AREAint = 1 },
          s"${RED}現在の範囲設定は 3×3 です".asMessageEffect(),
          open
        )
      )
    }

    @SuspendingMethod def computeButtonToToggleConsumingMineStack() = recomputedButton {
      val playerData = BuildAssist.playermap[uniqueId]
      val currentStatus = playerData.zs_minestack_flag

      val iconItemStackBuilder = IconItemStackBuilder(Material.CHEST)
        .title(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定: ${if (currentStatus) "ON" else "OFF"}")
        .lore(
          s"$RESET${GRAY}スキルでブロックを並べるとき",
          s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
          s"$RESET${GRAY}建築LV ${BuildAssist.config.getblocklineupMinestacklevel()} 以上で利用可能",
          s"$RESET${GRAY}クリックで切り替え"
        )
        .build()

      button.Button(
        iconItemStackBuilder,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          deferredEffect {
            if (playerData.level < BuildAssist.config.zoneskillMinestacklevel) {
              s"$RED\"建築LVが足りません\"".asMessageEffect()
            } else {
              sequentialEffect(
                targetedeffect.UnfocusedEffect { playerData.zs_minestack_flag = !currentStatus },
                s"MineStack優先設定${if (currentStatus) "OFF" else "ON"}".asMessageEffect(),
                open
              )
            }
          }
        )
      )
    }

    implicit case class PlayerDataOps(val playerData: PlayerData) extends AnyVal {
      @SuspendingMethod def computeCurrentSkillRange() = AREAint * 2 + 1
    }
  }
  
  @SuspendingMethod def computeMenuLayout(player: Player) = {
    import ConstantButtons._
    val computations = ButtonComputations(player)
    import computations._

    menuinventory.IndexedSlotLayout(
      0 -> buttonToOpenPreviousPage,
      4 -> computeButtonToToggleDirtPlacement(),
      13 -> computeButtonToShowCurrentStatus(),
      19 -> computeButtonToMaximizeRange(),
      20 -> computeButtonToIncreaseRange(),
      22 -> computeButtonToResetRange(),
      24 -> computeButtonToDecreaseRange(),
      25 -> computeButtonToMinimizeRange(),
      35 -> computeButtonToToggleConsumingMineStack()
    )
  }

  override val open: TargetedEffect[Player] = computedEffect { player =>
    val session = MenuInventoryView(4.rows(), s"$DARK_PURPLE${BOLD}「範囲設置スキル」設定画面").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        targetedeffect.UnfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }

}
