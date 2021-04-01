package com.github.unchama.buildassist.menu

import cats.effect.IO
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, onMainThread}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import net.md_5.bungee.api.ChatColor.GREEN
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Material, Sound}
/**
 * 直列設置メニュー
 */
case class BlockLinePlacementSkillMenu() extends Menu {
  /**
   * メニューを開く操作に必要な環境情報の型。
   * 例えば、メニューが利用するAPIなどをここを通して渡すことができる。
   */
  override type Environment = BlockLinePlacementSkillMenu.Environment

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE$BOLD「直列設置」設定")

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(opener: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    val openerData = BuildAssist.instance.temporaryData(opener.getUniqueId)
    val openSelf = environment.canOpenBlockLinePlacementSkillMenu.open(BlockLinePlacementSkillMenu())
    new MenuSlotLayout(
      Map(
        // ホームを開く
        27 -> {
          val item = new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
            .amount(1)
            .title(s"$YELLOW$UNDERLINE${BOLD}ホームへ")
            .lore(
              s"$RESET$DARK_RED${UNDERLINE}クリックで移動"
            )
            .build()
          val toHome = new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, environment.canOpenBuildMainMenu.open(BuildMainMenu))
          val button = new Button(item, List(toHome))
          button
        },
        //直列設置設定
        0 -> {
          val item = new IconItemStackBuilder(Material.WOOD)
            .amount(1)
            .title(s"$YELLOW$UNDERLINE${BOLD}直列設置 ：${BuildAssist.lineFillStateDescriptions(openerData.lineFillStatus)}")
            .lore(
              s"$RESET${GRAY}オフハンドに木の棒、メインハンドに設置したいブロックを持って",
              s"$RESET${GRAY}左クリックすると向いてる方向に並べて設置します。",
              s"$RESET${GRAY}建築Lv${BuildAssist.config.getLineFillUnlockLevel}以上で利用可能",
              s"$RESET${GRAY}クリックで切り替え"
            )
            .build()

          val effect = SequentialEffect(
            UnfocusedEffect {
              openerData.lineFillStatus = openerData.lineFillStatus.next
            },
            MessageEffect(s"${GREEN}直列設置 ：${BuildAssist.lineFillStateDescriptions(openerData.lineFillStatus)}"),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            openSelf
          )

          val shiftMode = new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, effect)
          val button = new Button(item, List(shiftMode))
          button
        },
        //直列設置ハーフブロック設定
        1 -> {
          val item = new IconItemStackBuilder(Material.STEP)
            .amount(1)
            .title(s"$YELLOW$UNDERLINE${BOLD}ハーフブロック設定 ：${BuildAssist.lineFillSlabPositionDescriptions(openerData.lineFillSlabPosition)}")
            .lore(
              s"$RESET${GRAY}ハーフブロックを並べる時の位置を決めます。",
              s"$RESET${GRAY}クリックで切り替え"
            )
            .build()

          val effect = SequentialEffect(
            UnfocusedEffect {
              openerData.lineFillSlabPosition = openerData.lineFillSlabPosition.next
            },
            MessageEffect(s"${GREEN}ハーフブロック設定 ：${BuildAssist.lineFillSlabPositionDescriptions(openerData.lineFillSlabPosition)}"),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            openSelf
          )

          val shiftMode = new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, effect)
          val button = new Button(item, List(shiftMode))
          button
        },
        //直列設置一部ブロックを破壊して並べる設定
        2 -> {
          val item = new IconItemStackBuilder(Material.TNT)
            .amount(1)
            .title(s"$YELLOW$UNDERLINE${BOLD}破壊設定 ：${BuildAssist.asDescription(openerData.lineFillDestructWeakBlocks)}")
            .lore(
              s"$RESET${GRAY}ブロックを並べるとき特定のブロックを破壊して並べます。",
              s"$RESET${GRAY}破壊対象ブロック：草,花,水,雪,松明,きのこ",
              s"$RESET${GRAY}クリックで切り替え"
            )
            .build()

          val effect = SequentialEffect(
            UnfocusedEffect {
              openerData.lineFillDestructWeakBlocks = !openerData.lineFillDestructWeakBlocks
            },
            MessageEffect(s"${GREEN}破壊設定 ：${BuildAssist.asDescription(openerData.lineFillDestructWeakBlocks)}"),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
            openSelf
          )

          val shiftMode = new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, effect)
          val button = new Button(item, List(shiftMode))
          button
        },
        //MineStackの方を優先して消費する設定
        8 -> {
          val item = new IconItemStackBuilder(Material.CHEST)
            .amount(1)
            .title(s"$YELLOW$UNDERLINE${BOLD}MineStack優先設定 ：${BuildAssist.asDescription(openerData.lineFillPrioritizeMineStack)}")
            .lore(
              s"$RESET${GRAY}スキルでブロックを並べるとき",
              s"$RESET${GRAY}MineStackの在庫を優先して消費します。",
              s"$RESET${GRAY}建築Lv${BuildAssist.config.getLineFillFromMineStackUnlockLevel}以上で利用可能",
              s"$RESET${GRAY}クリックで切り替え"
            )
            .build()

          val openerLevel = BuildAssist.instance.buildAmountDataRepository(opener).read.unsafeRunSync().levelCorrespondingToExp.level
          val effect = if (openerLevel < BuildAssist.config.getLineFillFromMineStackUnlockLevel) {
            MessageEffect(s"${RED}建築Lvが足りません")
          } else {
            SequentialEffect(
              UnfocusedEffect {
                openerData.lineFillPrioritizeMineStack = !openerData.lineFillPrioritizeMineStack
              },
              MessageEffect(s"${GREEN}マインスタック優先設定 ：${BuildAssist.asDescription(openerData.lineFillPrioritizeMineStack)}"),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
              openSelf
            )
          }

          val shiftMode = new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, effect)
          val button = new Button(item, List(shiftMode))
          button
        },
      )
    )
  }
}

object BlockLinePlacementSkillMenu {
  final case class Environment()(implicit val canOpenBuildMainMenu: CanOpen[IO, BuildMainMenu.type],
                                 val canOpenBlockLinePlacementSkillMenu: CanOpen[IO, BlockLinePlacementSkillMenu])
}