package com.github.unchama.buildassist.menu

import cats.effect.IO
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.data.PlayerData
import com.github.unchama.buildassist.repo.InMemoryBulkFillRangeRepo
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.{menuinventory, targetedeffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Bukkit, Material, Sound}

object BlockPlacementSkillMenu extends Menu {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}
  import com.github.unchama.targetedeffect._
  import menuinventory.syntax._

  private implicit class PlayerDataOps(val playerData: PlayerData) extends AnyVal {
    def computeCurrentSkillRange(): Int = InMemoryBulkFillRangeRepo.get(Bukkit.getPlayer(playerData.uuid))
  }

  override val frame: MenuFrame =
    MenuFrame(4.chestRows, s"$DARK_PURPLE$BOLD「範囲設置スキル」設定画面")

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    import ConstantButtons._
    val computations = ButtonComputations(player)
    import computations._

    val constantPart = Map(
      0 -> buttonToOpenPreviousPage
    )

    import cats.implicits._

    val dynamicPartComputation =
      List(
        4 -> computeButtonToToggleDirtPlacement(),
        13 -> computeButtonToShowCurrentStatus(),
        19 -> computeButtonToMaximizeRange(),
        20 -> computeButtonToIncreaseRange(),
        22 -> computeButtonToResetRange(),
        24 -> computeButtonToDecreaseRange(),
        25 -> computeButtonToMinimizeRange(),
        35 -> computeButtonToToggleConsumingMineStack()
      )
        .map(_.sequence)
        .sequence

    for {
      dynamicPart <- dynamicPartComputation
    } yield menuinventory.MenuSlotLayout(constantPart ++ dynamicPart)
  }

  private case class ButtonComputations(player: Player) extends AnyVal {

    import player._

    def computeButtonToToggleDirtPlacement(): IO[Button] = RecomputedButton {
      IO {
        val playerData = BuildAssist.playermap(getUniqueId)
        val currentStatus = playerData.fillSurface

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
              playerData.fillSurface = !currentStatus
            },
            // 切り替えた先のステータスを表示するのでおｋ
            MessageEffect(s"${RED}土設置機能${if (currentStatus) "OFF" else "ON"}")
          )
        )
      }
    }

    def computeButtonToShowCurrentStatus(): IO[Button] = RecomputedButton {
      IO {
        val playerData = BuildAssist.playermap(getUniqueId)
        val isSkillEnabled = playerData.isEnabledBulkBlockPlace
        val skillRange = playerData.computeCurrentSkillRange()
        val isConsumingMineStack = playerData.preferMineStackBool

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
      val playerData = BuildAssist.playermap(getUniqueId)
      val currentRange = InMemoryBulkFillRangeRepo.get(player)

      val iconItemStack = new SkullItemStackBuilder("MHF_ArrowUp")
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を最大値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： ${InMemoryBulkFillRangeRepo.max}×${InMemoryBulkFillRangeRepo.max}"
        )
        .amount(InMemoryBulkFillRangeRepo.max)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect {
            InMemoryBulkFillRangeRepo.update(player, InMemoryBulkFillRangeRepo.max)
          },
          MessageEffect(s"${RED}現在の範囲設定は ${InMemoryBulkFillRangeRepo.max}×${InMemoryBulkFillRangeRepo.max} です"),
          open
        )
      )
    }

    def computeButtonToIncreaseRange(): IO[Button] = IO {
      val playerData = BuildAssist.playermap(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = currentRange + 2

      val iconItemStack = new SkullItemStackBuilder("MHF_ArrowUp")
        .title(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階大きくする")
        .lore {
          List(s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange").concat(
            if (InMemoryBulkFillRangeRepo.get(player) == InMemoryBulkFillRangeRepo.max) {
              Seq(
                s"$RESET${RED}これ以上範囲設定を大きくできません。"
              )
            } else {
              Seq(
                s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
                s"$RESET$RED※範囲設定の最大値は${InMemoryBulkFillRangeRepo.max}×${InMemoryBulkFillRangeRepo.max}※"
              )
            }
          )
        }
        .amount(7)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          DeferredEffect(
            IO {
              if (InMemoryBulkFillRangeRepo.get(player) < InMemoryBulkFillRangeRepo.max)
                SequentialEffect(
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                  UnfocusedEffect {
                    InMemoryBulkFillRangeRepo.update(player, (_, i) => i + 2)
                  },
                  MessageEffect(s"${RED}現在の範囲設定は $changedRange×$changedRange です"),
                  open
                )
              else TargetedEffect.emptyEffect
            }
          )
        )
      )
    }

    def computeButtonToResetRange(): IO[Button] = IO {
      val playerData = BuildAssist.playermap(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()

      val iconItemStack = new SkullItemStackBuilder("MHF_TNT")
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を初期値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： ${InMemoryBulkFillRangeRepo.defaults}×${InMemoryBulkFillRangeRepo.defaults}"
        )
        .amount(5)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect {
            InMemoryBulkFillRangeRepo.update(player, InMemoryBulkFillRangeRepo.defaults)
          },
          MessageEffect(s"${RED}現在の範囲設定は ${InMemoryBulkFillRangeRepo.defaults}×${InMemoryBulkFillRangeRepo.defaults} です"),
          open
        )
      )
    }

    def computeButtonToDecreaseRange(): IO[Button] = IO {
      val playerData = BuildAssist.playermap(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()
      val changedRange = currentRange - 2

      val iconItemStack = new SkullItemStackBuilder("MHF_ArrowDown")
        .title(s"$YELLOW$UNDERLINE${BOLD}範囲設定を一段階小さくする")
        .lore(
          List(s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange").concat(
            if (InMemoryBulkFillRangeRepo.get(player) == InMemoryBulkFillRangeRepo.min) {
              List(
                s"${RED}これ以上範囲設定を小さくできません。"
              )
            } else {
              List(
                s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： $changedRange×$changedRange",
                s"$RESET$RED※範囲設定の最小値は${InMemoryBulkFillRangeRepo.min}×${InMemoryBulkFillRangeRepo.min}※"
              )
            }
          )
        )
        .amount(3)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          DeferredEffect(
            IO {
              if (InMemoryBulkFillRangeRepo.get(player) > InMemoryBulkFillRangeRepo.min)
                SequentialEffect(
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
                  UnfocusedEffect {
                    InMemoryBulkFillRangeRepo.update(player, (_, i) => i - 2)
                  },
                  MessageEffect(s"${RED}現在の範囲設定は $changedRange×$changedRange です"),
                  open
                )
              else TargetedEffect.emptyEffect
            }
          )
        )
      )
    }

    def computeButtonToMinimizeRange(): IO[Button] = IO {
      val playerData = BuildAssist.playermap(getUniqueId)
      val currentRange = playerData.computeCurrentSkillRange()

      val iconItemStack = new SkullItemStackBuilder("MHF_ArrowDown")
        .title(s"$RED$UNDERLINE${BOLD}範囲設定を最小値に変更")
        .lore(
          s"$RESET${AQUA}現在の範囲設定： $currentRange×$currentRange",
          s"$RESET$AQUA${UNDERLINE}変更後の範囲設定： ${InMemoryBulkFillRangeRepo.min}×${InMemoryBulkFillRangeRepo.min}"
        )
        .amount(1)
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          targetedeffect.UnfocusedEffect {
            InMemoryBulkFillRangeRepo.update(player, InMemoryBulkFillRangeRepo.min)
          },
          MessageEffect(s"${RED}現在の範囲設定は ${InMemoryBulkFillRangeRepo.min}×${InMemoryBulkFillRangeRepo.min} です"),
          open
        )
      )
    }

    def computeButtonToToggleConsumingMineStack(): IO[Button] = RecomputedButton {
      IO {
        val playerData = BuildAssist.playermap(getUniqueId)
        val currentStatus = playerData.preferMineStackBool

        val iconItemStackBuilder = new IconItemStackBuilder(Material.CHEST)
          .title(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定: ${if (currentStatus) "ON" else "OFF"}")
          .lore(
            s"$RESET${GRAY}スキルでブロックを並べるとき",
            s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
            s"$RESET${GRAY}建築LV ${BuildAssist.config.getLinearFillSkillPreferMineStackLevel} 以上で利用可能",
            s"$RESET${GRAY}クリックで切り替え"
          )
          .build()

        Button(
          iconItemStackBuilder,
          LeftClickButtonEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            DeferredEffect {
              IO {
                if (playerData.level < BuildAssist.config.getRangeFillSkillPreferMineStackLevel)
                  MessageEffect(s"${RED}建築LVが足りません")
                else
                  SequentialEffect(
                    targetedeffect.UnfocusedEffect {
                      playerData.preferMineStackBool = !currentStatus
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

  private object ConstantButtons {
    val buttonToOpenPreviousPage: Button = {
      val iconItemStack = new IconItemStackBuilder(Material.BARRIER)
        .title(s"$YELLOW$UNDERLINE${BOLD}元のページへ")
        .lore(s"$RESET$DARK_RED${UNDERLINE}クリックで移動")
        .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          BuildMainMenu.open
        )
      )
    }
  }
}
