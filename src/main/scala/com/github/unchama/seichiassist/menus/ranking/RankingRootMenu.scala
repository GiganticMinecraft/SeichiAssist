package com.github.unchama.seichiassist.menus.ranking

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.syntax._
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.stickmenu.{FirstPage, StickMenu}
import com.github.unchama.targetedeffect.player.PlayerEffects.openInventoryEffect
import eu.timepit.refined.auto._
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player

object RankingRootMenu extends Menu {
  class Environment(implicit val ioCanOpenFirstPage: IO CanOpen FirstPage.type,
                    val ioCanOpenSeichiRankingMenu: IO CanOpen SeichiRankingMenu,
                    val ioCanOpenBuildRankingMenu: IO CanOpen BuildRankingMenu,
                    val ioCanOpenLoginTimeRankingMenu: IO CanOpen LoginTimeRankingMenu,
                    val ioCanOpenVoteCountRankingMenu: IO CanOpen VoteCountRankingMenu
                   )
  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}ランキング")

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    import environment._

    val seichiGodRankingButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COOKIE)
          .title(s"$YELLOW$UNDERLINE${BOLD}整地神ランキングを見る")
          .lore(List(
            s"$RESET$RED(整地神ランキング150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          ioCanOpenSeichiRankingMenu.open(SeichiRankingMenu(0)),
        )
      )
    }

    val loginGodRankingButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COOKIE)
          .title(s"$YELLOW$UNDERLINE${BOLD}ログイン神ランキングを見る")
          .lore(List(
            s"$RESET$RED(ログイン神ランキング150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          ioCanOpenLoginTimeRankingMenu.open(LoginTimeRankingMenu(0))
        )
      )
    }

    val voteGodRankingButton: Button = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COOKIE)
          .title(s"$YELLOW$UNDERLINE${BOLD}投票神ランキングを見る")
          .lore(List(
            s"$RESET$RED(投票神ランキング150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          ioCanOpenVoteCountRankingMenu.open(VoteCountRankingMenu(0))
        )
      )
    }

    val buildGodRankingButton = {
      val iconItemStack =
        new IconItemStackBuilder(Material.COOKIE)
          .title(s"$YELLOW$UNDERLINE${BOLD}建築神ランキングを見る")
          .lore(List(
            s"$RESET$RED(建築神ランキング150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          ))
          .build()

      Button(
        iconItemStack,
        LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          ioCanOpenBuildRankingMenu.open(BuildRankingMenu(0))
        )
      )
    }

    MenuSlotLayout(
      ChestSlotRef(1, 1) -> seichiGodRankingButton,
      ChestSlotRef(1, 3) -> loginGodRankingButton,
      ChestSlotRef(1, 5) -> voteGodRankingButton,
      ChestSlotRef(1, 7) -> buildGodRankingButton,
      ChestSlotRef(3, 0) -> {
        CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
          "メインメニューへ",
          StickMenu.firstPage
        )
      }
    )
  }
}
