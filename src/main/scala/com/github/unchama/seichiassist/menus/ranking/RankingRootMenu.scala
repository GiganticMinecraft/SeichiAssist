package com.github.unchama.seichiassist.menus.ranking

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.syntax._
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.{LoginTime, VoteCount}
import eu.timepit.refined.auto._
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object RankingRootMenu extends Menu {
  class Environment(
    implicit val ioCanOpenFirstPage: IO CanOpen FirstPage.type,
    val ioCanOpenSeichiRankingMenu: IO CanOpen RankingMenu[SeichiAmountData],
    val ioCanOpenBuildRankingMenu: IO CanOpen RankingMenu[BuildAmountData],
    val ioCanOpenLoginTimeRankingMenu: IO CanOpen RankingMenu[LoginTime],
    val ioCanOpenVoteCountRankingMenu: IO CanOpen RankingMenu[VoteCount],
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  )

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}ランキング")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._

    def iconOf(rankingName: String): ItemStack =
      new IconItemStackBuilder(Material.COOKIE)
        .title(s"$YELLOW$UNDERLINE$BOLD${rankingName}を見る")
        .lore(
          List(
            s"$RESET$RED(${rankingName}150位以内のプレイヤーのみ表記されます)",
            s"$RESET$DARK_RED${UNDERLINE}クリックで開く"
          )
        )
        .build()

    val seichiGodRankingButton: Button = Button(
      iconOf("整地神ランキング"),
      LeftClickButtonEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        ioCanOpenSeichiRankingMenu.open(RankingMenu(RankingMenuTemplates.seichi))
      )
    )

    val loginGodRankingButton: Button = Button(
      iconOf("ログイン神ランキング"),
      LeftClickButtonEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        ioCanOpenLoginTimeRankingMenu.open(RankingMenu(RankingMenuTemplates.login))
      )
    )

    val voteGodRankingButton: Button = Button(
      iconOf("投票神ランキング"),
      LeftClickButtonEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        ioCanOpenVoteCountRankingMenu.open(RankingMenu(RankingMenuTemplates.vote))
      )
    )

    val buildGodRankingButton: Button = Button(
      iconOf("建築神ランキング"),
      LeftClickButtonEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        ioCanOpenBuildRankingMenu.open(RankingMenu(RankingMenuTemplates.build))
      )
    )

    IO.pure {
      MenuSlotLayout(
        ChestSlotRef(1, 1) -> seichiGodRankingButton,
        ChestSlotRef(1, 3) -> loginGodRankingButton,
        ChestSlotRef(1, 5) -> voteGodRankingButton,
        ChestSlotRef(1, 7) -> buildGodRankingButton,
        ChestSlotRef(3, 0) -> CommonButtons.openStickMenu
      )
    }
  }
}
