package com.github.unchama.seichiassist.menus

import arrow.core.Left
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.computedEffect
import com.github.unchama.targetedeffect.deferredEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
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

  private suspend fun Player.computeMenuLayout(): IndexedSlotLayout {
    val openerData = SeichiAssist.playermap[uniqueId]!!

    suspend fun computeStatsButton(): Button = Button(
        SkullItemStackBuilder(uniqueId)
            .title("$YELLOW$BOLD$UNDERLINE${name}の統計データ")
            .lore(PlayerInformationDescriptions.playerInfoLore(openerData))
            .build(),
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
          sequentialEffect(
              openerData.toggleExpBarVisibility(),
              deferredEffect {
                val toggleSoundPitch = if (openerData.expbar.isVisible) 1.0f else 0.5f
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, toggleSoundPitch)
              },
              deferredEffect { overwriteCurrentSlotBy(computeStatsButton()) }
          )
        }
    )

    suspend fun computeEffectSuppressionButton(): Button {
      val buttonLore: List<String> = run {
        val toggleNavigation = listOf(
            openerData.fastDiggingEffectSuppressor.currentStatus(),
            "$RESET$DARK_RED${UNDERLINE}クリックで" + openerData.fastDiggingEffectSuppressor.nextToggledStatus()
        )

        val explanation = listOf(
            "$RESET${GRAY}採掘速度上昇効果とは",
            "$RESET${GRAY}接続人数と1分間の採掘量に応じて",
            "$RESET${GRAY}採掘速度が変化するシステムです",
            "$RESET${GOLD}現在の採掘速度上昇Lv：${openerData.minespeedlv + 1}"
        )

        val effectStats =
            listOf("$RESET$YELLOW${UNDERLINE}上昇量の内訳") +
                openerData.effectdatalist.map { it.effectDescription }

        toggleNavigation + explanation + effectStats
      }

      return Button(
          IconItemStackBuilder(Material.DIAMOND_PICKAXE)
              .title("$YELLOW$UNDERLINE${BOLD}採掘速度上昇効果")
              .enchanted()
              .lore(buttonLore)
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                openerData.fastDiggingEffectSuppressor.suppressionDegreeToggleEffect,
                deferredEffect { openerData.computeFastDiggingEffect() },
                deferredEffect { overwriteCurrentSlotBy(computeEffectSuppressionButton()) }
            )
          }
      )
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun computeMineStackButton(): Button {
      val minimumLevelRequired = SeichiAssist.seichiAssistConfig.getMineStacklevel(1)
      val playerHasEnoughLevelToOpen = openerData.level >= minimumLevelRequired

      val buttonLore: List<String> = run {
        val explanation = listOf(
            "$RESET${GREEN}説明しよう!MineStackとは…",
            "${RESET}主要アイテムを無限にスタック出来る!",
            "${RESET}スタックしたアイテムは",
            "${RESET}ここから取り出せるゾ!"
        )

        val actionGuidance = if (playerHasEnoughLevelToOpen) {
          "$RESET$DARK_GREEN${UNDERLINE}クリックで開く"
        } else {
          "$RESET$DARK_RED${UNDERLINE}整地レベルが${minimumLevelRequired}以上必要です"
        }

        val annotation = listOf(
            "$RESET${DARK_GRAY}※スタックしたアイテムは",
            "$RESET${DARK_GRAY}各サバイバルサーバー間で",
            "$RESET${DARK_GRAY}共有されます"
        )

        explanation + actionGuidance + annotation
      }

      val leftClickEffect = if (playerHasEnoughLevelToOpen) {
        sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
            MineStackUI.open
        )
      } else FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)

      return Button(
          IconItemStackBuilder(Material.CHEST)
              .title("$YELLOW$UNDERLINE${BOLD}MineStack機能")
              .lore(buttonLore)
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)
      )
    }

    return IndexedSlotLayout(
        0 to computeStatsButton(),
        1 to computeEffectSuppressionButton(),
        24 to computeMineStackButton()
    )
  }

  val open: TargetedEffect<Player> = computedEffect { player ->
    val view = MenuInventoryView(Left(4 * 9), "${LIGHT_PURPLE}木の棒メニュー", player.computeMenuLayout())

    view.createNewSession().open
  }
}
