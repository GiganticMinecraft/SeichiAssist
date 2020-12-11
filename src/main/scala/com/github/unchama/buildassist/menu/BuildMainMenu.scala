package com.github.unchama.buildassist.menu

import cats.effect.{IO, SyncIO}
import com.github.unchama.buildassist.menu.BuildMainMenu.EMPHASIZE
import com.github.unchama.buildassist.{BuildAssist, MenuInventoryData}
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{Flying, NotFlying, RemainingFlyDuration}
import com.github.unchama.seichiassist.{SkullOwners, subsystems}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.PlayerEffects.{closeInventoryEffect, openInventoryEffect}
import com.github.unchama.targetedeffect.player.{CommandEffect, FocusedSoundEffect}
import com.github.unchama.targetedeffect.{ComputedEffect, DeferredEffect, SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.{Material, Sound}

private case class ButtonComputations(player: Player) extends AnyVal {

  import BuildMainMenu._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}
  import player._

  def computeNotationOfStats(): IO[Button] = RecomputedButton {
    IO {
      import java.math.RoundingMode
      val openerData = BuildAssist.playermap(getUniqueId)
      val iconItemStack = new SkullItemStackBuilder(getUniqueId)
        .enchanted()
        .title(s"$YELLOW$EMPHASIZE${openerData.name}の建築データ")
        .lore(
          s"$RESET${AQUA}建築Lv: ${openerData.level}",
          /* 小数点以下一桁で表示。
           * https://github.com/GiganticMinecraft/SeichiAssist/issues/540 対策。
           */
          s"$RESET${AQUA}総建築量: ${openerData.totalbuildnum.setScale(1, RoundingMode.HALF_UP).toPlainString}",
          s"$RESET$DARK_GRAY※1分毎に更新"
        )
        .build()

      Button(iconItemStack)
    }
  }

  def computeButtonToShowStateOfFlying()
                                      (implicit flySystem: StatefulSubsystem[subsystems.managedfly.InternalState[SyncIO]]): IO[Button] = {
    for {
      flyStatus <- flySystem.state.playerFlyDurations(player).read.toIO
    } yield {
      val flyStatusLoreLines = flyStatus match {
        case Flying(remainingDuration) =>
          List(
            s"$RESET${AQUA}Fly 効果: ON",
            remainingDuration match {
              case RemainingFlyDuration.Infinity =>
                s"$RESET${AQUA}Fly 残り時間: ∞"
              case RemainingFlyDuration.PositiveMinutes(minutes) =>
                s"$RESET${AQUA}Fly 残り時間: ${minutes}分"
            }
          )
        case NotFlying =>
          List(
            s"$RESET${AQUA}Fly 効果: OFF"
          )
      }

      val iconItemStack = new IconItemStackBuilder(Material.COOKED_CHICKEN)
        .title(s"$YELLOW${EMPHASIZE}Fly機能 情報表示")
        .lore(flyStatusLoreLines)
        .build()

      Button(iconItemStack)
    }
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
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            DeferredEffect {
              IO {
                if (openerData.level < BuildAssist.config.getZoneSetSkillLevel) {
                  MessageEffect(s"${RED}建築Lvが足りません")
                } else {
                  if (openerData.ZoneSetSkillFlag) SequentialEffect(
                    UnfocusedEffect {
                      openerData.ZoneSetSkillFlag = false
                    },
                    MessageEffect(s"${RED}範囲設置スキルOFF")
                  ) else SequentialEffect(
                    UnfocusedEffect {
                      openerData.ZoneSetSkillFlag = true
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

  def computeButtonToOpenRangedPlaceSkillMenu()
                                             (implicit flySystem: StatefulSubsystem[subsystems.managedfly.InternalState[SyncIO]]): IO[Button] = IO {
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
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
          DeferredEffect {
            IO {
              if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                MessageEffect(s"${RED}建築Lvが足りません")
              } else {
                new BlockPlacementSkillMenu().open
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
          DeferredEffect {
            IO {
              if (openerData.level < BuildAssist.config.getblocklineuplevel()) {
                MessageEffect(s"${RED}建築Lvが足りません")
              } else {
                SequentialEffect(
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                  UnfocusedEffect {
                    openerData.line_up_flg += 1
                    openerData.line_up_flg %= 3
                  },
                  DeferredEffect {
                    IO {
                      MessageEffect(s"${GREEN}ブロックを並べるスキル(仮): ${BuildAssist.line_up_str(openerData.line_up_flg)}")
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
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          ComputedEffect(p => openInventoryEffect(MenuInventoryData.getBlockLineUpData(p)))
        )
      }
    )
  }

  def computeButtonToOpenMenuToCraftItemsWhereMineStack()
                                                       (implicit flySystem: StatefulSubsystem[subsystems.managedfly.InternalState[SyncIO]]): IO[Button] = IO {
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

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.syncShift

  // TODO プレーヤーが飛行中かどうかでON/追加の表示を変えるとUX良さそう
  val buttonToFlyFor1Minute: Button = {
    val iconItemStack = new IconItemStackBuilder(Material.FEATHER)
      .amount(1)
      .title(s"$YELLOW${EMPHASIZE}Fly機能、ON$AQUA$EMPHASIZE(1分)")
      .lore(
        s"$RESET${YELLOW}クリックすると以降1分間に渡り",
        s"$RESET${YELLOW}経験値を消費しつつFlyが可能になります。",
        s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
      )
      .build()

    Button(
      iconItemStack,
      action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
        SequentialEffect(
          closeInventoryEffect,
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          CommandEffect("fly add 1")
        )
      }
    )
  }

  // TODO プレーヤーが飛行中かどうかでON/追加の表示を変えるとUX良さそう
  val buttonToFlyFor5Minutes: Button = {
    val iconItemStack = new IconItemStackBuilder(Material.FEATHER)
      .amount(5)
      .title(s"$YELLOW${EMPHASIZE}Fly機能、ON$AQUA$EMPHASIZE(5分)")
      .lore(
        s"$RESET${YELLOW}クリックすると以降5分間に渡り",
        s"$RESET${YELLOW}経験値を消費しつつFlyが可能になります。",
        s"$RESET$DARK_GREEN${UNDERLINE}必要経験値量: 毎分${BuildAssist.config.getFlyExp}"
      )
      .build()

    Button(
      iconItemStack,
      action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
        SequentialEffect(
          closeInventoryEffect,
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          CommandEffect("fly add 5")
        )
      }
    )
  }

  val buttonToFlyEndlessly: Button = {
    val iconItemStack = new IconItemStackBuilder(Material.ELYTRA)
      .title(s"$YELLOW${EMPHASIZE}Fly機能、ON$RED$EMPHASIZE(無制限)")
      .lore(
        s"$RESET${YELLOW}クリックすると以降OFFにするまで",
        s"$RESET${YELLOW}経験値を消費しつつFlyが可能になります。",
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
      .title(s"$YELLOW${EMPHASIZE}Fly機能、OFF")
      .lore(
        s"$RESET${RED}クリックすると、残り時間にかかわらず",
        s"$RESET${RED}Flyを終了します。"
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

class BuildMainMenu(implicit flySystem: StatefulSubsystem[subsystems.managedfly.InternalState[SyncIO]]) extends Menu {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.syncShift
  import menuinventory.syntax._

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"${LIGHT_PURPLE}木の棒メニューB")

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
}

object BuildMainMenu {
  val EMPHASIZE = s"$UNDERLINE$BOLD"
}
