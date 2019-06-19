package com.github.unchama.seichiassist.menus

import arrow.core.Left
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ButtonEffect
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.computedEffect
import com.github.unchama.targetedeffect.ops.asSequentialEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
object StickMenu {
  @Suppress("RedundantSuspendModifier")
  private suspend fun mineSpeedToggleButtonLore(operatorData: PlayerData): List<String> {
    val toggleNavigation = listOf(
        operatorData.fastDiggingEffectSuppressor.currentStatus(),
        "$RESET$DARK_RED${UNDERLINE}クリックで" + operatorData.fastDiggingEffectSuppressor.nextToggledStatus()
    )

    val explanation = listOf(
        "$RESET${GRAY}採掘速度上昇効果とは",
        "$RESET${GRAY}接続人数と1分間の採掘量に応じて",
        "$RESET${GRAY}採掘速度が変化するシステムです",
        "$RESET${GOLD}現在の採掘速度上昇Lv：${operatorData.minespeedlv + 1}"
    )

    val effectStats =
        listOf("$RESET$YELLOW${UNDERLINE}上昇量の内訳") +
            operatorData.effectdatalist.map { it.effectDescription }

    return toggleNavigation + explanation + effectStats
  }

  private suspend fun Player.computeMenuLayout(): IndexedSlotLayout {
    val openerData = SeichiAssist.playermap[uniqueId]!!

    suspend fun computeStatsButton(): Button = Button(
        SkullItemStackBuilder(uniqueId)
            .title("$YELLOW$BOLD$UNDERLINE${name}の統計データ")
            .lore(PlayerInformationDescriptions.playerInfoLore(openerData))
            .build(),
        ButtonEffect(ClickEventFilter.LEFT_CLICK) {
          listOf(
              openerData.toggleExpBarVisibility(),
              computedEffect {
                val toggleSoundPitch = if (openerData.expbar.isVisible) 1.0f else 0.5f
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, toggleSoundPitch)
              },
              computedEffect { overwriteCurrentSlotBy(computeStatsButton()) }
          ).asSequentialEffect()
        }
    )

    suspend fun computeEffectSuppressionButton(): Button = Button(
        IconItemStackBuilder(Material.DIAMOND_PICKAXE)
            .title("$YELLOW$UNDERLINE${BOLD}採掘速度上昇効果")
            .enchanted()
            .lore(mineSpeedToggleButtonLore(openerData))
            .build(),
        ButtonEffect(ClickEventFilter.LEFT_CLICK) {
          listOf(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              openerData.fastDiggingEffectSuppressor.toggleSuppressionDegree(),
              openerData.fastDiggingEffect(),
              computedEffect { overwriteCurrentViewBy(computeMenuLayout()) }
          ).asSequentialEffect()
        }
    )

    return IndexedSlotLayout(
        0 to computeStatsButton(),
        1 to computeEffectSuppressionButton()
    )
  }

  fun open(): TargetedEffect<Player> = TargetedEffect { player ->
    val view = MenuInventoryView(Left(4 * 9), "${LIGHT_PURPLE}木の棒メニュー", player.computeMenuLayout())

    view.createNewSession().openSessionInventoryEffect.runFor(player)
  }
}
