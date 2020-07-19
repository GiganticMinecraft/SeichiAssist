package com.github.unchama.buildassist.menu

import cats.effect.IO
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.data.MenuInventoryData
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.{CommandEffect, FocusedSoundEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.{Material, Sound}

object BuildMainMenu extends Menu {

  import com.github.unchama.menuinventory.slot.button.RecomputedButton
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}
  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.player.PlayerEffects._
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
        import java.math.RoundingMode
        val openerData = BuildAssist.playermap(getUniqueId)
        val iconItemStack = new SkullItemStackBuilder(getUniqueId)
          .enchanted()
          .title(s"$YELLOW$EMPHASIZE${openerData.name}の建築データ")
          .lore(
            s"$RESET${AQUA}建築レベル: ${openerData.level}",
            /* 小数点以下一桁で表示。
             * https://github.com/GiganticMinecraft/SeichiAssist/issues/540 対策。
             */
            s"$RESET${AQUA}総建築量: ${openerData.totalBuildCount.setScale(1).toPlainString}",
            s"$RESET$DARK_GRAY※1分毎に更新"
          )
          .build()

        Button(iconItemStack)
      }
    }

    def computeButtonToShowStateOfFlying(): IO[Button] = IO {
      val openerData = BuildAssist.playermap(getUniqueId)
      val iconItemStack = new IconItemStackBuilder(Material.COOKED_CHICKEN)
        .title(s"$YELLOW${EMPHASIZE}fly機能 情報表示")
        .lore(
          s"$RESET${AQUA}fly 効果: ${if (openerData.isFlying) "ON" else "OFF"}",
          s"$RESET${AQUA}fly 残り時間: ${if (openerData.doesEndlessFly) "∞" else openerData.flyMinute}"
        )
        .build()

      Button(iconItemStack)
    }

    def computeButtonToToggleRangedPlaceSkill(): IO[Button] = RecomputedButton(
      IO {
        val openerData = BuildAssist.playermap(getUniqueId)
        val iconItemStack = new IconItemStackBuilder(Material.STONE)
          .title(s"$GREEN$EMPHASIZE「範囲設置スキル」現在：${if (openerData.isEnabledBulkBlockPlace) "ON" else "OFF"}")
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
            SequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              DeferredEffect {
                IO {
                  if (openerData.level < BuildAssist.config.getZoneSetSkillLevel) {
                    MessageEffect(s"${RED}建築LVが足りません")
                  } else {
                    if (openerData.isEnabledBulkBlockPlace) SequentialEffect(
                      UnfocusedEffect {
                        openerData.isEnabledBulkBlockPlace = false
                      },
                      MessageEffect(s"${RED}範囲設置スキルOFF")
                    ) else SequentialEffect(
                      UnfocusedEffect {
                        openerData.isEnabledBulkBlockPlace = true
                      },
                      MessageEffect(s"${RED}範囲設置スキルON")
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
          s"$RESET${GRAY}MineStack優先設定:${if (openerData.preferMineStackZ) "ON" else "OFF"}"
        )
        .build()

      Button(iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
            DeferredEffect {
              IO {
                if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                  MessageEffect(s"${RED}建築LVが足りません")
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
          .title(s"$YELLOW${EMPHASIZE}ブロックを並べるスキル(仮): ${BuildAssist.lineFillFlag(openerData.lineFillFlag)}")
          .lore(
            s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
            s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()

        Button(iconItemStack,
          action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
            DeferredEffect {
              IO {
                if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                  MessageEffect(s"${RED}建築LVが足りません")
                } else {
                  SequentialEffect(
                    FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                    UnfocusedEffect {
                      openerData.lineFillFlag += 1
                      openerData.lineFillFlag %= 3
                    },
                    DeferredEffect {
                      IO {
                        MessageEffect(s"${GREEN}ブロックを並べるスキル(仮): ${BuildAssist.lineFillFlag(openerData.lineFillFlag)}")
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
          s"$RESET${GRAY}スキル設定: ${BuildAssist.lineFillFlag(openerData.lineFillFlag)}",
          s"$RESET${GRAY}ハーフブロック設定: ${BuildAssist.lineUpStepStr(openerData.lineUpStepFlag)}",
          s"$RESET${GRAY}破壊設定: ${BuildAssist.onOrOff(openerData.breakLightBlockFlag)}",
          s"$RESET${GRAY}MineStack優先設定: ${BuildAssist.onOrOff(openerData.preferMineStackI)}"
        )
        .build()

      Button(iconItemStack,
        FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            ComputedEffect(p => openInventoryEffect(MenuInventoryData.getBlockLineUpData(p)))
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
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            MineStackMassCraftMenu().open
          )
        }
      )
    }
  }

  private object ConstantButtons {
    val buttonToFlyFor1Minute: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.FEATHER)
        .amount(1)
        .title(s"$YELLOW${EMPHASIZE}fly機能、ON$AQUA$EMPHASIZE(1分)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降1分間に渡り",
          s"$RESET${YELLOW}経験値を消費しつつflyが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect("fly 1")
          )
        }
      )
    }

    val buttonToFlyFor5Minutes: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.FEATHER)
        .amount(5)
        .title(s"$YELLOW${EMPHASIZE}fly機能、ON$AQUA$EMPHASIZE(5分)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降5分間に渡り",
          s"$RESET${YELLOW}経験値を消費しつつflyが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect("fly 5")
          )
        }
      )
    }

    val buttonToFlyEndlessly: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.ELYTRA)
        .title(s"$YELLOW${EMPHASIZE}fly機能、ON$RED$EMPHASIZE(無制限)")
        .lore(
          s"$RESET${YELLOW}クリックすると以降OFFにするまで",
          s"$RESET${YELLOW}経験値を消費しつつflyが可能になります。",
          s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
        )
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect("fly endless")
          )
        }
      )
    }

    val buttonToTerminateFlight: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.CHAINMAIL_BOOTS)
        .title(s"$YELLOW${EMPHASIZE}fly機能、OFF")
        .lore(
          s"$RESET${RED}クリックすると、残り時間にかかわらず",
          s"$RESET${RED}flyを終了します。"
        )
        .flagged(ItemFlag.HIDE_ATTRIBUTES)
        .build()

      Button(
        iconItemStack,
        action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
          SequentialEffect(
            closeInventoryEffect,
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            CommandEffect("fly finish")
          )
        }
      )
    }
  }

}
