package com.github.unchama.seichiassist.menus

import arrow.core.Left
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ButtonAction
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions
import com.github.unchama.seichiassist.util.ops.lore
import kotlinx.coroutines.runBlocking
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
object StickMenu {
  private fun mineSpeedToggleButtonLore(operatorData: PlayerData): List<String> {
    val toggleNavigation = when (operatorData.effectflag) {
      0 -> listOf(
          "$RESET${GREEN}現在有効です(無制限)",
          "$RESET$DARK_RED${UNDERLINE}クリックで127制限"
      )
      1 -> listOf(
          "$RESET${GREEN}現在有効です$YELLOW(127制限)",
          "$RESET$DARK_RED${UNDERLINE}クリックで200制限"
      )
      2 -> listOf(
          "$RESET${GREEN}現在有効です$YELLOW(200制限)",
          "$RESET$DARK_RED${UNDERLINE}クリックで400制限"
      )
      3 -> listOf(
          "$RESET${GREEN}現在有効です$YELLOW(400制限)",
          "$RESET$DARK_RED${UNDERLINE}クリックで600制限"
      )
      4 -> listOf(
          "$RESET${GREEN}現在有効です$YELLOW(600制限)",
          "$RESET$DARK_RED${UNDERLINE}クリックでOFF"
      )
      else -> listOf(
          "$RESET${RED}現在OFFです",
          "$RESET$DARK_GREEN${UNDERLINE}クリックで無制限"
      )
    }

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

  fun Player.openMenu() {
    val openerData = SeichiAssist.playermap[uniqueId]!!

    val menuView = MenuInventoryView(
        Left(4 * 9), "${LIGHT_PURPLE}木の棒メニュー",
        mapOf(
            0 to Button(
                SkullItemStackBuilder(uniqueId)
                    .title("$YELLOW$BOLD$UNDERLINE${name}の統計データ")
                    .lore(PlayerInformationDescriptions.playerInfoLore(openerData))
                    .build(),
                ButtonAction(ClickEventFilter.LEFT_CLICK) { event ->
                  openerData.toggleExpBarVisibility()
                  openerData.notifyExpBarVisibility()

                  event.currentItem.lore = PlayerInformationDescriptions.playerInfoLore(openerData)
                }
            ),
            1 to Button(
                IconItemStackBuilder(Material.DIAMOND_PICKAXE)
                    .title("$YELLOW$UNDERLINE${BOLD}採掘速度上昇効果")
                    .enchanted()
                    .lore(mineSpeedToggleButtonLore(openerData))
                    .build(),
                ButtonAction(ClickEventFilter.LEFT_CLICK) { event ->
                  runBlocking {
                    // TODO 副作用全部一箇所にまとめたい

                    val effectResponse = openerData.toggleEffect()
                    effectResponse.transmitTo(this@openMenu)
                  }

                  player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

                  val activeEffects = openerData.effectdatalist

                  val amplifierSum = activeEffects.map { it.amplifier }.sum()
                  val maxDuration = activeEffects.map { it.duration }.max() ?: 0
                  val computedAmplifier = Math.floor(amplifierSum - 1).toInt()

                  val maxSpeed: Int = when(openerData.effectflag) {
                    0 -> 25565
                    1 -> 127
                    2 -> 200
                    3 -> 400
                    4 -> 600
                    else -> 0
                  }

                  // 実際に適用されるeffect量
                  val amplifier = Math.min(computedAmplifier, maxSpeed)

                  // 実際のeffect値が0より小さいときはeffectを適用しない
                  val potionEffect = if (amplifier < 0) {
                    PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false)
                  } else {
                    PotionEffect(PotionEffectType.FAST_DIGGING, maxDuration, amplifier, false, false)
                  }

                  player.addPotionEffect(potionEffect, true)

                  event.currentItem.lore = mineSpeedToggleButtonLore(openerData)
                }
            )
        )
    )

    openInventory(menuView.inventory)
  }
}
