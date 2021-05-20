package com.github.unchama.seichiassist.menus.nickname

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.{ClickEventFilter, FilteredButtonEffect}
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, onMainThread}
import com.github.unchama.seichiassist.menus.achievement.AchievementMenu
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.entity.Player
import org.bukkit.{ChatColor, Material, Sound}

/**
 * 二つ名組み合わせメニュー
 */
object NicknameCombineMenu extends Menu {
  class Environment(implicit val canOpenSelf: IO CanOpen NicknameCombineMenu.type,
                    val ioCanOpenAchievementMenu: IO CanOpen AchievementMenu.type)

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"${ChatColor.DARK_PURPLE}${ChatColor.BOLD}二つ名組合せシステム")

  override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    import eu.timepit.refined.auto._

    val playerdata = SeichiAssist.playermap(player.getUniqueId)
    val refreshAchievementPointButton = {
      def computeIt: Button = {
        val ap = playerdata.achievePoint
        val lore = List(
          s"${ChatColor.RESET}${ChatColor.RED}累計獲得量：${ap.cumulativeTotal}",
          s"${ChatColor.RESET}${ChatColor.RED}累計消費量：${ap.used}",
          s"${ChatColor.RESET}${ChatColor.AQUA}使用可能量：${ap.left}"
        )
        new Button(
          new IconItemStackBuilder(Material.EMERALD_ORE)
            .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}実績ポイント 情報")
            .lore(lore)
            .build(),
          List(
            FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(view =>
              SequentialEffect(
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
                UnfocusedEffect {
                  playerdata.recalculateAchievePoint()
                },
                UnfocusedEffect {
                  view.overwriteCurrentSlotBy(computeIt)
                }
              )
            )
          )
        )
      }

      computeIt
    }
    val openShopButton = {

      new Button(
        new IconItemStackBuilder(Material.ITEM_FRAME)
          .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}実績ポイントショップ")
          .lore(s"${ChatColor.RESET}${ChatColor.GREEN}クリックで開きます")
          .build(),
        List(
          FilteredButtonEffect(
            ClickEventFilter.LEFT_CLICK
          )(x => ???)
        )
      )

    }

    val checkCurrentNick = {
      val nickname = playerdata.settings.nickname
      val playerTitle = Nicknames.getTitleFor(nickname.id1, nickname.id2, nickname.id3)

      new Button(
        new IconItemStackBuilder(Material.BOOK)
          .title(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "現在の二つ名の確認")
          .lore(ChatColor.RESET + "" + ChatColor.RED + "「" + playerTitle + "」")
          .build(),
        List()
      )
    }

    val convertButton = {
      def computeIt: Button = {
        new Button(
          new IconItemStackBuilder(Material.EMERALD)
            .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}ポイント変換ボタン")
            .lore(
              s"${ChatColor.RESET}${ChatColor.RED}JMS投票で手に入るポイントを",
              s"${ChatColor.RESET}${ChatColor.RED}実績ポイントに変換できます。",
              s"${ChatColor.RESET}${ChatColor.YELLOW}${ChatColor.BOLD}投票pt 10pt → 実績pt 3pt",
              s"${ChatColor.RESET}${ChatColor.AQUA}クリックで変換を一回行います。",
              s"${ChatColor.RESET}${ChatColor.GREEN}所有投票pt :${playerdata.effectPoint}",
              s"${ChatColor.RESET}${ChatColor.GREEN}所有実績pt :${playerdata.achievePoint.left}"
            )
            .build(),
          List(
            FilteredButtonEffect(ClickEventFilter.LEFT_CLICK)(x => {
              val soundEff = FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
              val eff = soundEff :: (if (playerdata.effectPoint >= 10) {
                List(
                  UnfocusedEffect {
                    playerdata.convertEffectPointToAchievePoint()
                  },
                  UnfocusedEffect {
                    playerdata.recalculateAchievePoint()
                  },
                  UnfocusedEffect {
                    x.overwriteCurrentSlotBy(computeIt)
                  }
                )
              } else {
                List(MessageEffect("エフェクトポイントが不足しています。"))
              })

              SequentialEffect(eff)
            })
          )
        )
      }

      computeIt
    }

    val headSelect = Button(
      new IconItemStackBuilder(Material.WATER_BUCKET)
        .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}前パーツ選択画面")
        .lore(s"${ChatColor.RESET}${ChatColor.RED}クリックで移動します")
        .build(),
      // TODO pass IO CanOpen HeadPartSelectMenu
      new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, ???)
    )

    val middleSelect = Button(
      new IconItemStackBuilder(Material.MILK_BUCKET)
        .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}中パーツ選択画面")
        .lore(s"${ChatColor.RESET}${ChatColor.RED}クリックで移動します")
        .build(),
      // TODO pass IO CanOpen MiddlePartSelectMenu
      new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, ???)
    )

    val tailSelect = Button(
      new IconItemStackBuilder(Material.WATER_BUCKET)
        .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}後パーツ選択画面")
        .lore(s"${ChatColor.RESET}${ChatColor.RED}クリックで移動します")
        .build(),
      // TODO pass IO CanOpen TailPartSelectMenu
      new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, ???)
    )

    val backButton = Button(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft)
        .title(s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}${ChatColor.BOLD}実績・二つ名メニューへ")
        .lore(s"${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.UNDERLINE}クリックで移動")
        .build(),
      new FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, environment.ioCanOpenAchievementMenu.open(AchievementMenu))
    )

    MenuSlotLayout(
      ChestSlotRef(0, 0) -> refreshAchievementPointButton,
      ChestSlotRef(1, 0) -> openShopButton,
      ChestSlotRef(0, 1) -> convertButton,
      ChestSlotRef(0, 4) -> checkCurrentNick,
      ChestSlotRef(1, 2) -> headSelect,
      ChestSlotRef(1, 4) -> middleSelect,
      ChestSlotRef(1, 6) -> tailSelect,
      ChestSlotRef(3, 0) -> backButton
    )
  }
}
