package com.github.unchama.buildassist.menu

import cats.effect.IO
import com.github.unchama.buildassist.{BuildAssist, TemporaryMutableBuildAssistPlayerData}
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.BuildMainMenu
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{
  DeferredEffect,
  SequentialEffect,
  TargetedEffect,
  UnfocusedEffect
}
import com.github.unchama.{menuinventory, targetedeffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}

object BlockPlacementSkillMenu extends Menu {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
    asyncShift,
    onMainThread
  }
  import menuinventory.syntax._

  class Environment(implicit val canOpenMainMenu: CanOpen[IO, BuildMainMenu.type])

  override val frame: MenuFrame =
    MenuFrame(4.chestRows, s"$DARK_PURPLE$BOLD「範囲設置スキル」設定画面")

  def buttonToOpenPreviousPage(implicit environment: Environment): Button = {
    val iconItemStack = new IconItemStackBuilder(Material.BARRIER)
      .title(s"$YELLOW$UNDERLINE${BOLD}元のページへ")
      .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
      .build()

    Button(
      iconItemStack,
      LeftClickButtonEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        environment.canOpenMainMenu.open(BuildMainMenu)
      )
    )
  }

  private case class ButtonComputations(player: Player)(implicit environment: Environment) {

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
    import player._

    private def computeCurrentSkillAreaInt(range: Int): Int = (range - 1) / 2
    private val maxRange = 15
    private val maxAreaInt = computeCurrentSkillAreaInt(maxRange)
    private val minRange = 3
    private val minAreaInt = computeCurrentSkillAreaInt(minRange)

    implicit class PlayerDataOps(val playerData: TemporaryMutableBuildAssistPlayerData) {
      def computeCurrentSkillRange(): Int = playerData.AREAint * 2 + 1
    }

    def computeButtonToToggleDirtPlacement(): IO[Button] = RecomputedButton {
      IO {
        val playerData = BuildAssist.instance.temporaryData(getUniqueId)
        val currentStatus = playerData.zsSkillDirtFlag

        val iconItemStack = new IconItemStackBuilder(Material.DIRT)
          .title(s"$YELLOW$UNDERLINE${BOLD}設置時に下の空洞を埋める機能")
          .lore(
            s"$RESET$AQUA${UNDERLINE}機能の使用設定： ${if (currentStatus) "ON" else "OFF"}",
            s"$RESET$AQUA${UNDERLINE}機能の範囲： 地下5マスまで"
          )
          .build()

        Button(
          iconItemStack,
          LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            targetedeffect.UnfocusedEffect {
              playerData.zsSkillDirtFlag = !currentStatus
            },
            MessageEffect(s"${RED}土設置機能${if (currentStatus) "OFF" else "ON"}")
          )
        )
      }
    }

    def computeButtonToShowCurrentStatus(): IO[Button] = RecomputedButton {
      IO {
        val playerData = BuildAssist.instance.temporaryData(getUniqueId)
        val isSkillEnabled = playerData.ZoneSetSkillFlag
        val skillRange = playerData.computeCurrentSkillRange()
        val isConsumingMineStack = playerData.zs_minestack_flag

        val iconItemStack = new IconItemStackBuilder(Material.STONE)
          .title(s"$YELLOW$UNDERLINE${BOLD}現在の設定は以下の通りです")
          .lore(
            s"$RESET$AQUA${UNDERLINE}スキルの使用設定: ${if (isSkillEnabled) "ON" else "OFF"}",
            s"$RESET$AQUA${UNDERLINE}スキルの範囲設定: $skillRange×$skillRange",
            s"$RESET$AQUA${UNDERLINE}MineStack優先設定: ${if (isConsumingMineStack) "ON" else "OFF"}"
          )
          .build()

        Button(iconItemStack)
      }
    }

    def computeButtonToMaximizeRange(): IO[Button] = IO {
      val playerData = BuildAssist.instance.temporaryData(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()

      val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp)
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を最大値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $maxRange×$maxRange"
        )
        .amount(maxRange)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect {
            playerData.AREAint = maxAreaInt
          },
          MessageEffect(s"${RED}現在の範囲設定は $maxRange×$maxRange です"),
          open
        )
      )
    }

    def computeButtonToIncreaseRangeTo11(): IO[Button] = IO {
      val playerData = BuildAssist.instance.temporaryData(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = 11

      val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp)
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を$changedRange×${changedRange}に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange"
        )
        .amount(changedRange)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect {
            playerData.AREAint = computeCurrentSkillAreaInt(changedRange)
          },
          MessageEffect(s"${RED}現在の範囲設定は $changedRange×$changedRange です"),
          open
        )
      )
    }

    def computeButtonToIncreaseRange(): IO[Button] = IO {
      val playerData = BuildAssist.instance.temporaryData(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = currentRange + 2

      val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp)
        .title(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階大きくする")
        .lore {
          List(s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange").concat(
            if (playerData.AREAint == maxAreaInt) {
              Seq(s"$RESET${RED}これ以上範囲設定を大きくできません。")
            } else {
              Seq(
                s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
                s"$RESET$RED※範囲設定の最大値は$maxRange×$maxRange※"
              )
            }
          )
        }
        .amount(maxAreaInt)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(DeferredEffect(IO {
          if (playerData.AREAint < maxAreaInt)
            SequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              UnfocusedEffect {
                playerData.AREAint += 1
              },
              MessageEffect(s"${RED}現在の範囲設定は $changedRange×$changedRange です"),
              open
            )
          else TargetedEffect.emptyEffect
        }))
      )
    }

    def computeButtonToResetRange(): IO[Button] = IO {
      val playerData = BuildAssist.instance.temporaryData(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = 5

      val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_TNT)
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を初期値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange"
        )
        .amount(changedRange)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect {
            playerData.AREAint = computeCurrentSkillAreaInt(changedRange)
          },
          MessageEffect(s"${RED}現在の範囲設定は $changedRange×$changedRange です"),
          open
        )
      )
    }

    def computeButtonToDecreaseRange(): IO[Button] = IO {
      val playerData = BuildAssist.instance.temporaryData(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = currentRange - 2

      val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_ArrowDown)
        .title(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階小さくする")
        .lore(
          List(s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange")
            .concat(if (playerData.AREAint == minAreaInt) {
              List(s"${RED}これ以上範囲設定を小さくできません。")
            } else {
              List(
                s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
                s"$RESET$RED※範囲設定の最小値は$minRange×$minRange※"
              )
            })
        )
        .amount(minRange)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(DeferredEffect(IO {
          if (playerData.AREAint > minAreaInt)
            SequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              UnfocusedEffect {
                playerData.AREAint -= 1
              },
              MessageEffect(s"${RED}現在の範囲設定は $changedRange×$changedRange です"),
              open
            )
          else TargetedEffect.emptyEffect
        }))
      )
    }

    def computeButtonToMinimizeRange(): IO[Button] = IO {
      val playerData = BuildAssist.instance.temporaryData(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()

      val iconItemStack = new SkullItemStackBuilder(SkullOwners.MHF_ArrowDown)
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を最小値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $minRange×$minRange"
        )
        .amount(minAreaInt)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect {
            playerData.AREAint = minAreaInt
          },
          MessageEffect(s"${RED}現在の範囲設定は $minRange×$minRange です"),
          open
        )
      )
    }

    def computeButtonToToggleConsumingMineStack(): IO[Button] = RecomputedButton {
      BuildAssist.instance.buildAmountDataRepository(player).read.toIO.flatMap { amountData =>
        IO {
          val playerData = BuildAssist.instance.temporaryData(getUniqueId)

          val currentStatus = playerData.zs_minestack_flag

          val iconItemStackBuilder = new IconItemStackBuilder(Material.CHEST)
            .title(
              s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定: ${if (currentStatus) "ON" else "OFF"}"
            )
            .lore(
              s"$RESET${GRAY}スキルでブロックを並べるとき",
              s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
              s"$RESET${GRAY}建築Lv ${BuildAssist.config.getblocklineupMinestacklevel} 以上で利用可能",
              s"$RESET${GRAY}クリックで切り替え"
            )
            .build()

          Button(
            iconItemStackBuilder,
            LeftClickButtonEffect(
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              DeferredEffect {
                IO {
                  if (
                    amountData
                      .levelCorrespondingToExp
                      .level < BuildAssist.config.getZoneskillMinestacklevel
                  )
                    MessageEffect(s"${RED}建築Lvが足りません")
                  else
                    SequentialEffect(
                      targetedeffect.UnfocusedEffect {
                        playerData.zs_minestack_flag = !currentStatus
                      },
                      MessageEffect(s"MineStack優先設定${if (currentStatus) "OFF" else "ON"}"),
                      open
                    )
                }
              }
            )
          )
        }
      }
    }
  }

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    val computations = ButtonComputations(player)
    import computations._

    val constantPart = Map(0 -> buttonToOpenPreviousPage)

    import cats.implicits._

    val dynamicPartComputation =
      List(
        4 -> computeButtonToToggleDirtPlacement(),
        13 -> computeButtonToShowCurrentStatus(),
        19 -> computeButtonToMaximizeRange(),
        20 -> computeButtonToIncreaseRangeTo11(),
        21 -> computeButtonToIncreaseRange(),
        22 -> computeButtonToResetRange(),
        23 -> computeButtonToDecreaseRange(),
        24 -> computeButtonToMinimizeRange(),
        35 -> computeButtonToToggleConsumingMineStack()
      ).traverse(_.sequence)

    for {
      dynamicPart <- dynamicPartComputation
    } yield menuinventory.MenuSlotLayout(constantPart ++ dynamicPart)
  }
}
