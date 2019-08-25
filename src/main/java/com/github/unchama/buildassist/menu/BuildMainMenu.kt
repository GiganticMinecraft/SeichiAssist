package com.github.unchama.buildassist.menu

import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.MenuInventoryData
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.rows
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.menuinventory.slot.button.recomputedButton
import com.github.unchama.seichiassist.Schedulers
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.player.asCommandEffect
import com.github.unchama.targetedeffect.player.closeInventoryEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import java.util.*

private object BuildMainMenu : Menu {

  private object ConstantButtons {
  }

  private object ButtonComputations {

    suspend fun Player.computeStatsButton(): Button = recomputedButton {
      val openerData = BuildAssist.playermap[uniqueId]!!
      val iconItemStack = SkullItemStackBuilder(uniqueId)
          .enchanted()
          .title("$YELLOW$EMPHASIZE${openerData.name}の建築データ")
          .lore(
              "$RESET${AQUA}建築レベル: ${openerData.level}",
              "$RESET${AQUA}総建築量: ${openerData.totalbuildnum.toDouble()}",
              "$RESET${DARK_GRAY}※1分毎に更新"
          )
          .build()

      Button(iconItemStack)
    }

    suspend fun Player.computeButtonToShowStateOfFlying() = run {
      val openerData = BuildAssist.playermap[uniqueId]!!
      val iconItemStack = IconItemStackBuilder(Material.COOKED_CHICKEN)
          .title("$YELLOW${EMPHASIZE}FLY機能 情報表示")
          .lore(
              "$RESET${AQUA}FLY 効果: ${openerData.flyflag}",
              "$RESET${AQUA}FLY 残り時間: ${if (openerData.Endlessfly) "∞" else openerData.flytime}"
          )
          .build()

      Button(iconItemStack)
    }

    val flyFor1MinuteButton = run {
      val iconItemStack = IconItemStackBuilder(Material.FEATHER)
          .amount(1)
          .title("$YELLOW${EMPHASIZE}FLY機能、ON$AQUA$EMPHASIZE(1分)")
          .lore(
              "$RESET${YELLOW}クリックすると以降1分間に渡り",
              "$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
              "$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.flyExp}"
          )
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
            sequentialEffect(
                closeInventoryEffect,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                "fly 1".asCommandEffect()
            )
          }
      )
    }

    val flyFor5MinutesButton = run {
      val iconItemStack = IconItemStackBuilder(Material.FEATHER)
          .amount(5)
          .title("$YELLOW${EMPHASIZE}FLY機能、ON$AQUA$EMPHASIZE(5分)")
          .lore(
              "$RESET${YELLOW}クリックすると以降5分間に渡り",
              "$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
              "$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.flyExp}"
          )
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
            sequentialEffect(
                closeInventoryEffect,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                "fly 5".asCommandEffect()
            )
          }
      )
    }

    val flyEndlesslyButton = run {
      val iconItemStack = IconItemStackBuilder(Material.ELYTRA)
          .title("$YELLOW${EMPHASIZE}FLY機能、ON$RED$EMPHASIZE(無制限)")
          .lore(
              "$RESET${YELLOW}クリックすると以降OFFにするまで",
              "$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
              "$RESET$DARK_GREEN${UNDERLINE}必要経験値量：毎分 ${BuildAssist.config.flyExp}"
          )
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
            sequentialEffect(
                closeInventoryEffect,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                "fly endless".asCommandEffect()
            )
          }
      )
    }

    val terminateFlightButton = run {
      val iconItemStack = IconItemStackBuilder(Material.CHAINMAIL_BOOTS)
          .title("$YELLOW${EMPHASIZE}FLY機能、OFF")
          .lore(
              "$RESET${RED}クリックすると、残り時間にかかわらず",
              "$RESET${RED}FLYを終了します。"
          )
          .flagged(ItemFlag.HIDE_ATTRIBUTES)
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
            sequentialEffect(
                closeInventoryEffect,
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                "fly finish".asCommandEffect()
            )
          }
      )
    }

    suspend fun Player.computeButtonToToggleRangedPlaceSkill() = recomputedButton {
      val openerData = BuildAssist.playermap[uniqueId]!!
      val iconItemStack = IconItemStackBuilder(Material.STONE)
          .title("$GREEN${EMPHASIZE}「範囲設置スキル」現在：${if (openerData.zs_minestack_flag) "ON" else "OFF"}")
          .lore(
              "$RESET${YELLOW}「スニーク+左クリック」をすると、",
              "$RESET${YELLOW}オフハンドに持っているブロックと同じ物を",
              "$RESET${YELLOW}インベントリ内から消費し設置します。",
              "$RESET${LIGHT_PURPLE}＜クリックでON/OFF切り替え＞",
              "$RESET${GRAY}建築LV${BuildAssist.config.zoneSetSkillLevel}以上で利用可能"
          )
          .build()

      Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                deferredEffect {
                  if (openerData.level < BuildAssist.config.zoneSetSkillLevel) {
                    "${RED}建築LVが足りません".asMessageEffect()
                  } else {
                    if (openerData.ZoneSetSkillFlag) sequentialEffect(
                        unfocusedEffect { openerData.ZoneSetSkillFlag = false },
                        "${RED}範囲設置スキルOFF".asMessageEffect()
                    ) else sequentialEffect(
                        unfocusedEffect { openerData.ZoneSetSkillFlag = true },
                        "${RED}範囲設置スキルON".asMessageEffect()
                    )
                  }
                }
            )
          }
      )
    }

    suspend fun Player.computeButtonToOpenRangedPlaceSkillMenu() = run {
      val openerData = BuildAssist.playermap[uniqueId]!!
      val iconItemStack = SkullItemStackBuilder(UUID.fromString("d3c47f6fae3a45c1ad7ce2c762b03ae6"))
          .title("$YELLOW${EMPHASIZE}「範囲設置スキル」設定画面へ")
          .lore(
              "$RESET$DARK_RED${UNDERLINE}クリックで移動",
              "$RESET${GRAY}現在の設定",
              "$RESET${GRAY}MineStack優先設定:${if (openerData.zs_minestack_flag) "ON" else "OFF"}"
          )
          .build()

      Button(iconItemStack,
          FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
            sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
                deferredEffect {
                  if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                    "${RED}建築LVが足りません".asMessageEffect()
                  } else {
                    unfocusedEffect { openInventory(MenuInventoryData.getSetBlockSkillData(player)) }
                  }
                }
            )
          }
      )
    }

  }

  private suspend fun Player.computeMenuLayout(): IndexedSlotLayout =
      with(ConstantButtons) {
        with(ButtonComputations) {
          IndexedSlotLayout(
              0 to computeStatsButton(),
              2 to computeButtonToShowStateOfFlying(),
              3 to flyFor1MinuteButton,
              4 to flyFor5MinutesButton,
              5 to flyEndlesslyButton,
              6 to terminateFlightButton,
              18 to computeButtonToToggleRangedPlaceSkill(),
              19 to computeButtonToOpenRangedPlaceSkillMenu()
          )
        }
      }

  override val open: TargetedEffect<Player> = computedEffect { player ->
    val session = MenuInventoryView(4.rows(), "${LIGHT_PURPLE}木の棒メニュー").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        unfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }

  private val EMPHASIZE = "$UNDERLINE$BOLD"

}