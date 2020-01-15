package com.github.unchama.buildassist.menu

import cats.effect.IO
import com.github.unchama.buildassist.{BuildAssist, MenuInventoryData}
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.{Material, Sound}

object BuildMainMenu extends Menu {

  import com.github.unchama.menuinventory.slot.button.RecomputedButton
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}
  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.player.PlayerEffects._
  import com.github.unchama.targetedeffect.syntax._
  import menuinventory.syntax._

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"${LIGHT_PURPLE}木の棒メニューB")

  private val EMPHASIZE = s"$UNDERLINE$BOLD"

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    import ConstantButtons._
    val computations = ButtonComputations(player)
    import computations._
    val constantPart = Map(
      3 -> buttonToFlyFor1Minute,
      4 -> buttonToFlyFor5Minutes,
      5 -> buttonToFlyEndlessly,
      6 -> buttonToTerminateFlight
    )

    import cats.implicits._

    val dynamicPartComputation: IO[List[(Int, Button)]] =
      List(
        0 -> computeNotationOfStats(),
        2 -> computeButtonToShowStateOfFlying(),
        18 -> computeButtonToToggleRangedPlaceSkill(),
        19 -> computeButtonToOpenRangedPlaceSkillMenu(),
        27 -> computeButtonToLineUpBlocks(),
        28 -> computeButtonToOpenLineUpBlocksMenu(),
        35 -> computeButtonToOpenMenuToCraftItemsWhereMineStack()
      )
        .map(_.sequence)
        .sequence

    for {
      dynamicPart <- dynamicPartComputation
    } yield menuinventory.MenuSlotLayout(constantPart ++ dynamicPart)
  }

  private case class ButtonComputations(player: Player) extends AnyVal {

    import player._

    def computeNotationOfStats(): IO[Button] = RecomputedButton {
      IO {
        val openerData = BuildAssist.playermap(getUniqueId)
        val iconItemStack = new SkullItemStackBuilder(getUniqueId)
          .enchanted()
          .title(s"$YELLOW$EMPHASIZE${openerData.name}の建築データ")
          .lore(
            s"$RESET${AQUA}建築レベル: ${openerData.level}",
            s"$RESET${AQUA}総建築量: ${openerData.totalbuildnum.doubleValue()}",
            s"$RESET$DARK_GRAY※1分毎に更新"
          )
          .build()

        Button(iconItemStack)
      }
    }

    def computeButtonToShowStateOfFlying(): IO[Button] = IO {
      val openerData = BuildAssist.playermap(getUniqueId)
      val iconItemStack = new IconItemStackBuilder(Material.COOKED_CHICKEN)
        .title(s"$YELLOW${EMPHASIZE}FLY機能 情報表示")
        .lore(
          s"$RESET${AQUA}FLY 効果: ${if (openerData.flyflag) "ON" else "OFF"}",
          s"$RESET${AQUA}FLY 残り時間: ${if (openerData.endlessfly) "∞" else openerData.flytime}"
        )
        .build()

      Button(iconItemStack)
    }

    def computeButtonToToggleRangedPlaceSkill(): IO[Button] = RecomputedButton(
      IO {
        val openerData = BuildAssist.playermap(getUniqueId)
        val iconItemStack = new IconItemStackBuilder(Material.STONE)
          .title(s"$GREEN$EMPHASIZE「範囲設置スキル」現在：${if (openerData.ZoneSetSkillFlag) "ON" else "OFF"}")
          .lore(
            s"$RESET$YELLOW「スニーク+左クリック」をすると、",
            s"$RESET${YELLOW}オフハンドに持っているブロックと同じ物を",
            s"$RESET${YELLOW}インベントリ内から消費し設置します。",
            s"$RESET$LIGHT_PURPLE＜クリックでON/OFF切り替え＞"
          )
          .build()

        Button(
          iconItemStack,
          FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
            sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              deferredEffect {
                IO {
                  if (openerData.level < BuildAssist.config.getZoneSetSkillLevel) {
                    s"${RED}建築LVが足りません".asMessageEffect()
                  } else {
                    if (openerData.ZoneSetSkillFlag) sequentialEffect(
                      UnfocusedEffect {
                        openerData.ZoneSetSkillFlag = false
                      },
                      s"${RED}範囲設置スキルOFF".asMessageEffect()
                    ) else sequentialEffect(
                      UnfocusedEffect {
                        openerData.ZoneSetSkillFlag = true
                      },
                      s"${RED}範囲設置スキルON".asMessageEffect()
                    )
                  }
                }
              }
            )
          }
        )
      }
    )

    def computeButtonToOpenRangedPlaceSkillMenu(): IO[Button] = IO {
      val openerData = BuildAssist.playermap(getUniqueId)
      val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_Exclamation)
        .title(s"$YELLOW$EMPHASIZE「範囲設置スキル」設定画面へ")
        .lore(
          s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
          s"$RESET${GRAY}現在の設定",
          s"$RESET${GRAY}MineStack優先設定:${if (openerData.zs_minestack_flag) "ON" else "OFF"}"
        )
        .build()

      Button(iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
            deferredEffect {
              IO {
                if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                  s"${RED}建築LVが足りません".asMessageEffect()
                } else {
                  BlockPlacementSkillMenu.open
                }
              }
            }
          )
        }
      )
    }

    def computeButtonToLineUpBlocks(): IO[Button] = RecomputedButton(
      IO {
        val openerData = BuildAssist.playermap(getUniqueId)
        val iconItemStack = new IconItemStackBuilder(Material.WOOD)
          .title(s"$YELLOW${EMPHASIZE}ブロックを並べるスキル(仮): ${BuildAssist.line_up_str(openerData.line_up_flg)}")
          .lore(
            s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
            s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()

        Button(iconItemStack,
          action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
            deferredEffect {
              IO {
                if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                  s"${RED}建築LVが足りません".asMessageEffect()
                } else {
                  sequentialEffect(
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                    UnfocusedEffect {
                      openerData.line_up_flg += 1
                      openerData.line_up_flg %= 3
                    },
                    deferredEffect {
                      IO {
                        s"${GREEN}ブロックを並べるスキル(仮): ${BuildAssist.line_up_str(openerData.line_up_flg)}".asMessageEffect()
                      }
                    }
                  )
                }
              }
            }
          }
        )
      }
    )

    def computeButtonToOpenLineUpBlocksMenu(): IO[Button] = IO {
      val openerData = BuildAssist.playermap(getUniqueId)
      val iconItemStack = new IconItemStackBuilder(Material.PAPER)
        .title(s"$YELLOW$EMPHASIZE「ブロックを並べるスキル（仮） 」設定画面へ")
        .lore(
          s"$RESET${GRAY}現在の設定",
          s"$RESET${GRAY}スキル設定: ${BuildAssist.line_up_str(openerData.line_up_flg)}",
          s"$RESET${GRAY}ハーフブロック設定: ${BuildAssist.line_up_step_str(openerData.line_up_step_flg)}",
          s"$RESET${GRAY}破壊設定: ${BuildAssist.line_up_off_on_str(openerData.line_up_des_flg)}",
          s"$RESET${GRAY}MineStack優先設定: ${BuildAssist.line_up_off_on_str(openerData.line_up_minestack_flg)}"
        )
        .build()

      Button(iconItemStack,
        FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            computedEffect(p => openInventoryEffect(MenuInventoryData.getBlockLineUpData(p)))
          )
        }
      )
    }

    def computeButtonToOpenMenuToCraftItemsWhereMineStack(): IO[Button] = IO {
      val iconItemStackBuilder = new IconItemStackBuilder(Material.WORKBENCH)
        .title(s"$YELLOW${EMPHASIZE}MineStackブロック一括クラフト画面へ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()

      Button(iconItemStackBuilder,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            computedEffect(p => openInventoryEffect(MenuInventoryData.getBlockCraftData(p)))
          )
        }
      )
    }
  }

  private object ConstantButtons {
    val buttonToFlyFor1Minute: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.FEATHER)
        .amount(1)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、ON$AQUA$EMPHASIZE(1分)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降1分間に渡り",
          s"$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly 1".asCommandEffect()
          )
        }
      )
    }

    val buttonToFlyFor5Minutes: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.FEATHER)
        .amount(5)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、ON$AQUA$EMPHASIZE(5分)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降5分間に渡り",
          s"$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly 5".asCommandEffect()
          )
        }
      )
    }

    val buttonToFlyEndlessly: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.ELYTRA)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、ON$RED$EMPHASIZE(無制限)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降OFFにするまで",
          s"$RESET${YELLOW}経験値を消費しつつFLYが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly endless".asCommandEffect()
          )
        }
      )
    }

    val buttonToTerminateFlight: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.CHAINMAIL_BOOTS)
        .title(s"$YELLOW${EMPHASIZE}FLY機能、OFF")
        .lore(
          s"$RESET${RED}クリックすると、残り時間にかかわらず",
          s"$RESET${RED}FLYを終了します。"
        )
        .flagged(ItemFlag.HIDE_ATTRIBUTES)
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          sequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            "fly finish".asCommandEffect()
          )
        }
      )
    }
  }

}
