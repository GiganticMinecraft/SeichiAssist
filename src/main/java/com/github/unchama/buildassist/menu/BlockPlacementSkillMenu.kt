package com.github.unchama.buildassist.menu

import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.PlayerData
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.rows
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.recomputedButton
import com.github.unchama.seichiassist.CommonSoundEffects
import com.github.unchama.seichiassist.Schedulers
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

object BlockPlacementSkillMenu : Menu {

  private val buttonToOpenPreviousPage = run {
    val iconItemStack = IconItemStackBuilder(Material.BARRIER)
        .title("$YELLOW$UNDERLINE${BOLD}元のページへ")
        .lore("$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()

    Button(
        iconItemStack,
        LeftClickButtonEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            BuildMainMenu.open
        )
    )
  }

  private suspend fun Player.computeButtonToToggleDirtPlacement() = recomputedButton {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val currentStatus = playerData.zsSkillDirtFlag

    val iconItemStack = IconItemStackBuilder(Material.DIRT)
        .title("$YELLOW$UNDERLINE${BOLD}設置時に下の空洞を埋める機能")
        .lore(
            "$RESET$AQUA${UNDERLINE}機能の使用設定： ${if (currentStatus) "ON" else "OFF"}",
            "$RESET$AQUA${UNDERLINE}機能の範囲： 地下5マスまで"
        )
        .build()

    Button(
        iconItemStack,
        LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            unfocusedEffect { playerData.zsSkillDirtFlag = !currentStatus },
            "${RED}土設置機能${if (currentStatus) "OFF" else "ON"}".asMessageEffect()
        )
    )
  }

  private suspend fun Player.computeButtonToShowCurrentStatus() = recomputedButton {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val isSkillEnabled = playerData.ZoneSetSkillFlag
    val skillRange = playerData.computeCurrentSkillRange()
    val isConsumingMineStack = playerData.zs_minestack_flag

    val iconItemStack = IconItemStackBuilder(Material.STONE)
        .title("$YELLOW$UNDERLINE${BOLD}現在の設定は以下の通りです")
        .lore(
            "$RESET$AQUA${UNDERLINE}スキルの使用設定: ${if (isSkillEnabled) "ON" else "OFF"}",
            "$RESET$AQUA${UNDERLINE}スキルの範囲設定: $skillRange×$skillRange",
            "$RESET$AQUA${UNDERLINE}MineStack優先設定: ${if (isConsumingMineStack) "ON" else "OFF"}"
        )
        .build()

    Button(iconItemStack)
  }

  private fun Player.computeButtonToMaximizeRange() = run {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val currentRange = playerData.computeCurrentSkillRange()

    val iconItemStack = SkullItemStackBuilder("MHF_ArrowUp")
        .title("$RED$UNDERLINE${BOLD}範囲設定を最大値に変更")
        .lore(
            "$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
            "$RESET$AQUA${UNDERLINE}変更後の範囲設定： 11×11"
        )
        .amount(11)
        .build()

    Button(
        iconItemStack,
        LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            unfocusedEffect { playerData.AREAint = 5 },
            "${RED}現在の範囲設定は 11×11 です".asMessageEffect(),
            open
        )
    )
  }

  private fun Player.computeButtonToIncreaseRange() = run {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val currentRange = playerData.computeCurrentSkillRange()
    val changedRange = currentRange + 2

    val iconItemStack = SkullItemStackBuilder("MHF_ArrowUp")
        .title("$YELLOW$UNDERLINE${BOLD}範囲設定を一段階大きくする")
        .lore(
            listOf(
                "$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange"
            ) +
                if (playerData.AREAint == 5) {
                  listOf(
                      "$RESET${RED}これ以上範囲設定を大きくできません。"
                  )
                } else {
                  listOf(
                      "$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
                      "$RESET${RED}※範囲設定の最大値は11×11※"
                  )
                }
        )
        .amount(7)
        .build()

    Button(
        iconItemStack,
        LeftClickButtonEffect(
            deferredEffect {
              if (playerData.AREAint < 5)
                sequentialEffect(
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                    unfocusedEffect { playerData.AREAint++ },
                    "${RED}現在の範囲設定は $changedRange×$changedRange です".asMessageEffect(),
                    open
                )
              else EmptyEffect
            }
        )
    )
  }

  private fun Player.computeButtonToResetRange() = run {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val currentRange = playerData.computeCurrentSkillRange()

    val iconItemStack = SkullItemStackBuilder("MHF_TNT")
        .title("$RED$UNDERLINE${BOLD}範囲設定を初期値に変更")
        .lore(
            "$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
            "$RESET$AQUA${UNDERLINE}変更後の範囲設定： 5×5"
        )
        .amount(5)
        .build()

    Button(
        iconItemStack,
        LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            unfocusedEffect { playerData.AREAint = 2 },
            "${RED}現在の範囲設定は 5×5 です".asMessageEffect(),
            open
        )
    )
  }

  private fun Player.computeButtonToDecreaseRange() = run {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val currentRange = playerData.computeCurrentSkillRange()
    val changedRange = currentRange + -2

    val iconItemStack = SkullItemStackBuilder("MHF_ArrowDown")
        .title("$YELLOW$UNDERLINE${BOLD}範囲設定を一段階小さくする")
        .lore(
            listOf(
                "$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange"
            ) +
                if (playerData.AREAint == 1) {
                  listOf(
                      "${RED}これ以上範囲設定を小さくできません。"
                  )
                } else {
                  listOf(
                      "$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
                      "$RESET${RED}※範囲設定の最大値は3×3※"
                  )
                }
        )
        .amount(3)
        .build()

    Button(
        iconItemStack,
        LeftClickButtonEffect(
            deferredEffect {
              if (playerData.AREAint > 1)
                sequentialEffect(
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                    unfocusedEffect { playerData.AREAint-- },
                    "${RED}現在の範囲設定は $changedRange×$changedRange です".asMessageEffect(),
                    open
                )
              else EmptyEffect
            }
        )
    )
  }

  private fun Player.computeButtonToMinimizeRange() = run {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val currentRange = playerData.computeCurrentSkillRange()

    val iconItemStack = SkullItemStackBuilder("MHF_ArrowDown")
        .title("$RED$UNDERLINE${BOLD}範囲設定を最小値に変更")
        .lore(
            "$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
            "$RESET$AQUA${UNDERLINE}変更後の範囲設定： 3×3"
        )
        .amount(1)
        .build()

    Button(
        iconItemStack,
        LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            unfocusedEffect { playerData.AREAint = 1 },
            "${RED}現在の範囲設定は 3×3 です".asMessageEffect(),
            open
        )
    )
  }

  private suspend fun Player.computeButtonToToggleConsumingMineStack() = recomputedButton {
    val playerData = BuildAssist.playermap[uniqueId]!!
    val currentStatus = playerData.zs_minestack_flag

    val iconItemStackBuilder = IconItemStackBuilder(Material.CHEST)
        .title("$YELLOW$UNDERLINE${BOLD}MineStack優先設定: ${if (currentStatus) "ON" else "OFF"}")
        .lore(
            "$RESET${GRAY}スキルでブロックを並べるとき",
            "$RESET${GRAY}MineStackの在庫を優先して消費します。",
            "$RESET${GRAY}建築LV ${BuildAssist.config.getblocklineupMinestacklevel()} 以上で利用可能",
            "$RESET${GRAY}クリックで切り替え"
        )
        .build()

    Button(
        iconItemStackBuilder,
        LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            deferredEffect {
              if (playerData.level < BuildAssist.config.zoneskillMinestacklevel) {
                "$RED\"建築LVが足りません\"".asMessageEffect()
              } else {
                sequentialEffect(
                    unfocusedEffect { playerData.zs_minestack_flag = !currentStatus },
                    "MineStack優先設定${if (currentStatus) "OFF" else "ON"}".asMessageEffect(),
                    open
                )
              }
            }
        )
    )
  }

  private fun PlayerData.computeCurrentSkillRange() = AREAint * 2 + 1

  private suspend fun Player.computeMenuLayout() = IndexedSlotLayout(
      0 to buttonToOpenPreviousPage,
      4 to computeButtonToToggleDirtPlacement(),
      13 to computeButtonToShowCurrentStatus(),
      19 to computeButtonToMaximizeRange(),
      20 to computeButtonToIncreaseRange(),
      22 to computeButtonToResetRange(),
      24 to computeButtonToDecreaseRange(),
      25 to computeButtonToMinimizeRange(),
      35 to computeButtonToToggleConsumingMineStack()
  )

  override val open: TargetedEffect<Player> = computedEffect { player ->
    val session = MenuInventoryView(4.rows(), "$DARK_PURPLE${BOLD}「範囲設置スキル」設定画面").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        unfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }

}
