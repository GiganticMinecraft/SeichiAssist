package com.github.unchama.buildassist.menu

import com.github.unchama.buildassist.{BuildAssist, MenuInventoryData}
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{IndexedSlotLayout, Menu, MenuInventoryView}
import com.github.unchama.seichiassist.{Schedulers, SkullOwners}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import com.github.unchama.{menuinventory, targetedeffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.{Material, Sound}

object BuildMainMenu extends Menu {
  private object ConstantButtons {
    val buttonToFlyFor1Minute = run {
      val iconItemStack = IconItemStackBuilder(Material.FEATHER)
        .amount(1)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、ON$AQUA$EMPHASIZE(1分)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降1分間に渡り",
          s"$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.flyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly 1".asCommandEffect()
          )
        }
      )
    }

    val buttonToFlyFor5Minutes = run {
      val iconItemStack = IconItemStackBuilder(Material.FEATHER)
        .amount(5)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、ON$AQUA$EMPHASIZE(5分)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降5分間に渡り",
          s"$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.flyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly 5".asCommandEffect()
          )
        }
      )
    }

    val buttonToFlyEndlessly = run {
      val iconItemStack = IconItemStackBuilder(Material.ELYTRA)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、ON$RED$EMPHASIZE(無制限)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降OFFにするまで",
          s"$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.flyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly endless".asCommandEffect()
          )
        }
      )
    }

    val buttonToTerminateFlight = run {
      val iconItemStack = IconItemStackBuilder(Material.CHAINMAIL_BOOTS)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、OFF")
        .lore(
          s"$RESET${RED}クリックすると、残り時間にかかわらず",
          s"$RESET${RED}FLYを終了します。"
        )
        .flagged(ItemFlag.HIDE_ATTRIBUTES)
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly finish".asCommandEffect()
          )
        }
      )
    }
  }

  private case class ButtonComputations(val player: Player) extends AnyVal {

    @SuspendingMethod def computeNotationOfStats(): Button = recomputedButton {
      val openerData = BuildAssist.playermap(uniqueId)
      val iconItemStack = SkullItemStackBuilder(uniqueId)
        .enchanted()
        .title(s"$YELLOW$EMPHASIZE${openerData.name}の建築データ")
        .lore(
          s"$RESET${AQUA}建築レベル: ${openerData.level}",
          s"$RESET${AQUA}総建築量: ${openerData.totalbuildnum.toDouble()}",
          s"$RESET${DARK_GRAY}※1分毎に更新"
        )
        .build()

      Button(iconItemStack)
    }

    @SuspendingMethod def computeButtonToShowStateOfFlying() = run {
      val openerData = BuildAssist.playermap(uniqueId)
      val iconItemStack = IconItemStackBuilder(Material.COOKED_CHICKEN)
        .title(s"$YELLOW${EMPHASIZE}FLY機能 情報表示")
        .lore(
          s"$RESET${AQUA}FLY 効果: ${if (openerData.flyflag) "ON" else "OFF"}",
          s"$RESET${AQUA}FLY 残り時間: ${openerData.flytime.toString().takeUnless { openerData.endlessfly } ?: "∞"}"
        )
        .build()

      Button(iconItemStack)
    }

    @SuspendingMethod def computeButtonToToggleRangedPlaceSkill() = recomputedButton {
      val openerData = BuildAssist.playermap(uniqueId)
      val iconItemStack = IconItemStackBuilder(Material.STONE)
        .title(s"$GREEN${EMPHASIZE}「範囲設置スキル」現在：${if (openerData.ZoneSetSkillFlag) "ON" else "OFF"}")
        .lore(
          s"$RESET${YELLOW}「スニーク+左クリック」をすると、",
          s"$RESET${YELLOW}オフハンドに持っているブロックと同じ物を",
          s"$RESET${YELLOW}インベントリ内から消費し設置します。",
          s"$RESET${LIGHT_PURPLE}＜クリックでON/OFF切り替え＞"
        )
        .build()

      Button(
        iconItemStack,
        FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            deferredEffect {
              if (openerData.level < BuildAssist.config.zoneSetSkillLevel) {
                s"${RED}建築LVが足りません".asMessageEffect()
              } else {
                if (openerData.ZoneSetSkillFlag) sequentialEffect(
                  UnfocusedEffect { openerData.ZoneSetSkillFlag = false },
                  s"${RED}範囲設置スキルOFF".asMessageEffect()
                ) else sequentialEffect(
                  UnfocusedEffect { openerData.ZoneSetSkillFlag = true },
                  s"${RED}範囲設置スキルON".asMessageEffect()
                )
              }
            }
          )
        }
      )
    }

    @SuspendingMethod def computeButtonToOpenRangedPlaceSkillMenu() = run {
      val openerData = BuildAssist.playermap(uniqueId)
      val iconItemStack = SkullItemStackBuilder(SkullOwners.MHF_Exclamation)
        .title(s"$YELLOW${EMPHASIZE}「範囲設置スキル」設定画面へ")
        .lore(
          s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
          s"$RESET${GRAY}現在の設定",
          s"$RESET${GRAY}MineStack優先設定:${if (openerData.zs_minestack_flag) "ON" else "OFF"}"
        )
        .build()

      Button(iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
            deferredEffect {
              if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                s"${RED}建築LVが足りません".asMessageEffect()
              } else {
                BlockPlacementSkillMenu.open
              }
            }
          )
        }
      )
    }

    @SuspendingMethod def computeButtonToLineUpBlocks() = recomputedButton {
      val openerData = BuildAssist.playermap(uniqueId)
      val iconItemStack = IconItemStackBuilder(Material.WOOD)
        .title(s"$YELLOW${EMPHASIZE}ブロックを並べるスキル(仮): ${BuildAssist.line_up_str[openerData.line_up_flg]}")
        .lore(
          s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
          s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
          s"$RESET${GRAY}クリックで切り替え"
        )
        .build()

      Button(iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          deferredEffect {
            if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
              s"${RED}建築LVが足りません".asMessageEffect()
            } else {
              sequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                TargetedEffect {
                  openerData.line_up_flg += 1
                  openerData.line_up_flg %= 3
                },
                deferredEffect { s"${GREEN}ブロックを並べるスキル(仮): ${BuildAssist.line_up_str[openerData.line_up_flg]}".asMessageEffect() }
              )
            }
          }
        }
      )
    }

    @SuspendingMethod def computeButtonToOpenLineUpBlocksMenu() = run {
      val openerData = BuildAssist.playermap(uniqueId)
      val iconItemStack = IconItemStackBuilder(Material.PAPER)
        .title(s"$YELLOW${EMPHASIZE}「ブロックを並べるスキル（仮） 」設定画面へ")
        .lore(
          s"$RESET${GRAY}現在の設定",
          s"$RESET${GRAY}スキル設定: ${BuildAssist.line_up_str[openerData.line_up_flg]}",
          s"$RESET${GRAY}ハーフブロック設定: ${BuildAssist.line_up_step_str[openerData.line_up_step_flg]}",
          s"$RESET${GRAY}破壊設定: ${BuildAssist.line_up_off_on_str[openerData.line_up_des_flg]}",
          s"$RESET${GRAY}MineStack優先設定: ${BuildAssist.line_up_off_on_str[openerData.line_up_minestack_flg]}"
        )
        .build()

      Button(iconItemStack,
        FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            TargetedEffect { it.openInventory(MenuInventoryData.blockLineUpData(it)) }
          )
        }
      )
    }

    @SuspendingMethod def computeButtonToOpenMenuToCraftItemsWhereMineStack() = run {
      val iconItemStackBuilder = IconItemStackBuilder(Material.WORKBENCH)
        .title(s"$YELLOW${EMPHASIZE}MineStackブロック一括クラフト画面へ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()

      Button(iconItemStackBuilder,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
          sequentialEffect(FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            TargetedEffect { it.openInventory(MenuInventoryData.blockCraftData(it)) })
        })
    }
  }

  private @SuspendingMethod def computeMenuLayout(player: Player): IndexedSlotLayout = {
    import ConstantButtons._

    val computations = ButtonComputations(player)

    import computations._

    menuinventory.IndexedSlotLayout(
      0 -> computeNotationOfStats(),
      2 -> computeButtonToShowStateOfFlying(),
      3 -> buttonToFlyFor1Minute,
      4 -> buttonToFlyFor5Minutes,
      5 -> buttonToFlyEndlessly,
      6 -> buttonToTerminateFlight,
      18 -> computeButtonToToggleRangedPlaceSkill(),
      19 -> computeButtonToOpenRangedPlaceSkillMenu(),
      27 -> computeButtonToLineUpBlocks(),
      28 -> computeButtonToOpenLineUpBlocksMenu(),
      35 -> computeButtonToOpenMenuToCraftItemsWhereMineStack()
    )
  }

  override val open: TargetedEffect[Player] = computedEffect { player =>
    val session = MenuInventoryView(4.rows(), s"${LIGHT_PURPLE}木の棒メニューB").createNewSession()

    sequentialEffect(
        session.openEffectThrough(Schedulers.sync),
        targetedeffect.UnfocusedEffect { session.overwriteViewWith(player.computeMenuLayout()) }
    )
  }

  private val EMPHASIZE = s"$UNDERLINE$BOLD"

}
