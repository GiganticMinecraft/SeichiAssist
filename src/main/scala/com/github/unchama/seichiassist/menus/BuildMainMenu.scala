package com.github.unchama.seichiassist.menus

import cats.effect.{IO, SyncIO}
import com.github.unchama.buildassist.menu.{BlockPlacementSkillMenu, MineStackMassCraftMenu}
import com.github.unchama.buildassist.{BuildAssist, MenuInventoryData}
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.{
  ClickEventFilter,
  FilteredButtonEffect
}
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.BuildMainMenu.EMPHASIZE
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{
  Flying,
  NotFlying,
  RemainingFlyDuration
}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.PlayerEffects.{
  closeInventoryEffect,
  openInventoryEffect
}
import com.github.unchama.targetedeffect.player.{CommandEffect, FocusedSoundEffect}
import com.github.unchama.targetedeffect.{
  ComputedEffect,
  DeferredEffect,
  SequentialEffect,
  UnfocusedEffect
}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.{Material, Sound}

private case class ButtonComputations(player: Player)(
  implicit ioOnMainThread: OnMinecraftServerThread[IO]
) {

  import BuildMainMenu._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import player._

  def computeStatsButton(): IO[Button] = RecomputedButton {
    BuildAssist.instance.buildAmountDataRepository(player).read.toIO.map { data =>
      val buildLevel = data.levelCorrespondingToExp
      val rawLevel = buildLevel.level

      val lore = {
        val alwaysDisplayedInfo = List(
          s"$RESET${AQUA}建築Lv: $rawLevel",
          s"$RESET${AQUA}総建築量: ${data.expAmount.toPlainString}"
        )

        // 最大レベルに到達した後は”次のレベル”が存在しないため、表示しない
        val nextLevelInfo: Option[String] = data
          .levelProgress
          .map(blp => s"$RESET${AQUA}次のレベルまで: ${blp.expAmountToNextLevel.toPlainString}")

        alwaysDisplayedInfo ++ nextLevelInfo
      }

      Button {
        new SkullItemStackBuilder(getUniqueId)
          .enchanted()
          .title(s"$YELLOW$EMPHASIZE${player.getName}の建築データ")
          .lore(lore)
          .build()
      }
    }
  }

  def computeButtonToShowStateOfFlying(
    implicit flyApi: ManagedFlyApi[SyncIO, Player]
  ): IO[Button] = {
    for {
      flyStatus <- flyApi.playerFlyDurations(player).read.toIO
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
          List(s"$RESET${AQUA}Fly 効果: OFF")
      }

      val iconItemStack = new IconItemStackBuilder(Material.COOKED_CHICKEN)
        .title(s"$YELLOW${EMPHASIZE}Fly機能 情報表示")
        .lore(flyStatusLoreLines)
        .build()

      Button(iconItemStack)
    }
  }

  def computeButtonToToggleRangedPlaceSkill(): IO[Button] = RecomputedButton(
    BuildAssist
      .instance
      .buildAmountDataRepository(player)
      .read
      .toIO
      .flatMap(amountData =>
        IO {
          val openerData = BuildAssist.instance.temporaryData(getUniqueId)
          val openerLevel = amountData.levelCorrespondingToExp.level

          val iconItemStack = new IconItemStackBuilder(Material.STONE)
            .title(
              s"$GREEN$EMPHASIZE「範囲設置スキル」現在：${if (openerData.ZoneSetSkillFlag) "ON" else "OFF"}"
            )
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
                    if (openerLevel < BuildAssist.config.getZoneSetSkillLevel) {
                      MessageEffect(s"${RED}建築Lvが足りません")
                    } else {
                      if (openerData.ZoneSetSkillFlag)
                        SequentialEffect(
                          UnfocusedEffect {
                            openerData.ZoneSetSkillFlag = false
                          },
                          MessageEffect(s"${RED}範囲設置スキルOFF")
                        )
                      else
                        SequentialEffect(
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
  )

  def computeButtonToOpenRangedPlaceSkillMenu(
    implicit canOpenBlockPlacementSkillMenu: CanOpen[IO, BlockPlacementSkillMenu.type]
  ): IO[Button] =
    BuildAssist.instance.buildAmountDataRepository(player).read.toIO.flatMap { amountData =>
      IO {
        val openerData = BuildAssist.instance.temporaryData(getUniqueId)

        val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_Exclamation)
          .title(s"$YELLOW$EMPHASIZE「範囲設置スキル」設定画面へ")
          .lore(
            s"$RESET$DARK_RED${UNDERLINE}クリックで移動",
            s"$RESET${GRAY}現在の設定",
            s"$RESET${GRAY}MineStack優先設定:${if (openerData.zs_minestack_flag) "ON" else "OFF"}"
          )
          .build()

        Button(
          iconItemStack,
          action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
            SequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f),
              DeferredEffect {
                IO {
                  if (
                    amountData
                      .levelCorrespondingToExp
                      .level < BuildAssist.config.getblocklineuplevel
                  ) {
                    MessageEffect(s"${RED}建築Lvが足りません")
                  } else {
                    canOpenBlockPlacementSkillMenu.open(BlockPlacementSkillMenu)
                  }
                }
              }
            )
          }
        )
      }
    }

  def computeButtonToLineUpBlocks(): IO[Button] = RecomputedButton(
    BuildAssist.instance.buildAmountDataRepository(player).read.toIO.flatMap { amountData =>
      IO {
        val openerData = BuildAssist.instance.temporaryData(getUniqueId)

        val iconItemStack = new IconItemStackBuilder(Material.WOOD)
          .title(s"$YELLOW${EMPHASIZE}直列設置: ${BuildAssist.line_up_str(openerData.line_up_flg)}")
          .lore(
            s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
            s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()

        Button(
          iconItemStack,
          action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
            DeferredEffect {
              IO {
                if (
                  amountData
                    .levelCorrespondingToExp
                    .level < BuildAssist.config.getblocklineuplevel
                ) {
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
                        MessageEffect(
                          s"${GREEN}直列設置: ${BuildAssist.line_up_str(openerData.line_up_flg)}"
                        )
                      }
                    }
                  )
                }
              }
            }
          }
        )
      }
    }
  )

  def computeButtonToOpenLineUpBlocksMenu(): IO[Button] = IO {
    val openerData = BuildAssist.instance.temporaryData(getUniqueId)

    val iconItemStack = new IconItemStackBuilder(Material.PAPER)
      .title(s"$YELLOW$EMPHASIZE「直列設置 」設定画面へ")
      .lore(
        s"$RESET${GRAY}現在の設定",
        s"$RESET${GRAY}スキル設定: ${BuildAssist.line_up_str(openerData.line_up_flg)}",
        s"$RESET${GRAY}ハーフブロック設定: ${BuildAssist.line_up_step_str(openerData.line_up_step_flg)}",
        s"$RESET${GRAY}破壊設定: ${BuildAssist.line_up_off_on_str(openerData.line_up_des_flg)}",
        s"$RESET${GRAY}MineStack優先設定: ${BuildAssist.line_up_off_on_str(openerData.line_up_minestack_flg)}"
      )
      .build()

    Button(
      iconItemStack,
      FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          ComputedEffect(p => openInventoryEffect(MenuInventoryData.getBlockLineUpData(p)))
        )
      }
    )
  }

  def computeButtonToOpenMenuToCraftItemsWhereMineStack(
    implicit canOpenMassCraftMenu: CanOpen[IO, MineStackMassCraftMenu]
  ): IO[Button] = IO {
    val iconItemStackBuilder = new IconItemStackBuilder(Material.WORKBENCH)
      .title(s"$YELLOW${EMPHASIZE}MineStackブロック一括クラフト画面へ")
      .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
      .build()

    Button(
      iconItemStackBuilder,
      action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          canOpenMassCraftMenu.open(MineStackMassCraftMenu())
        )
      }
    )
  }
}

private object ConstantButtons {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread

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
      .lore(s"$RESET${RED}クリックすると、残り時間にかかわらず", s"$RESET${RED}Flyを終了します。")
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

object BuildMainMenu extends Menu {

  import menuinventory.syntax._

  class Environment(
    implicit val flyApi: ManagedFlyApi[SyncIO, Player],
    val ioOnMainThread: OnMinecraftServerThread[IO],
    val canOpenBlockPlacementSkillMenu: CanOpen[IO, BlockPlacementSkillMenu.type],
    val canOpenMassCraftMenu: CanOpen[IO, MineStackMassCraftMenu]
  )

  val EMPHASIZE = s"$UNDERLINE$BOLD"

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"${LIGHT_PURPLE}木の棒メニューB")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import ConstantButtons._
    import environment._

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
        0 -> computeStatsButton(),
        2 -> computeButtonToShowStateOfFlying,
        18 -> computeButtonToToggleRangedPlaceSkill(),
        19 -> computeButtonToOpenRangedPlaceSkillMenu,
        27 -> computeButtonToLineUpBlocks(),
        28 -> computeButtonToOpenLineUpBlocksMenu(),
        35 -> computeButtonToOpenMenuToCraftItemsWhereMineStack
      ).traverse(_.sequence)

    for {
      dynamicPart <- dynamicPartComputation
    } yield menuinventory.MenuSlotLayout(constantPart ++ dynamicPart)
  }
}
