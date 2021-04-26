package com.github.unchama.seichiassist.menus.ranking

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.syntax._
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.buildranking.domain.{BuildRanking, BuildRankingRecord}
import com.github.unchama.seichiassist.subsystems.loginranking.domain.{LoginTimeRanking, LoginTimeRankingRecord}
import com.github.unchama.seichiassist.subsystems.ranking.RankingApi
import eu.timepit.refined.auto._
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object LoginTimeRankingMenu {
  class Environment(implicit val ioCanOpenRankingRootMenu: IO CanOpen RankingRootMenu.type,
                    val ioCanOpenLoginTimeRankingMenu: IO CanOpen LoginTimeRankingMenu,
                    val loginTimeRankingApi: RankingApi[IO, LoginTimeRanking]
                   )
}

case class LoginTimeRankingMenu(pageIndex: Int) extends Menu {
  private val perPage = 45
  private val cutoff = 150

  override type Environment = LoginTimeRankingMenu.Environment

  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}ログイン神ランキング")

  private def uiOperationSection(totalNumberOfPages: Int)
                                (implicit environment: Environment): Seq[(Int, Button)] = {
    import environment._

    def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(skullOwnerReference),
        s"ログイン神ランキング${pageIndex + 1}ページ目へ",
        LoginTimeRankingMenu(pageIndex)
      )

    val goBackToStickMenuSection =
      Seq(
        ChestSlotRef(5, 0) -> CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
          "ランキングメニューへ戻る",
          RankingRootMenu
        )
      )

    val previousPageButtonSection =
      if (pageIndex > 0)
        Seq(ChestSlotRef(5, 7) -> buttonToTransferTo(pageIndex - 1, SkullOwners.MHF_ArrowUp))
      else
        Seq()

    val nextPageButtonSection =
      if (pageIndex + 1 < totalNumberOfPages)
        Seq(ChestSlotRef(5, 8) -> buttonToTransferTo(pageIndex + 1, SkullOwners.MHF_ArrowDown))
      else
        Seq()

    goBackToStickMenuSection ++ previousPageButtonSection ++ nextPageButtonSection
  }

  private def rankingSection(ranking: LoginTimeRanking): Seq[(Int, Button)] = {
    def entry(position: Int, record: LoginTimeRankingRecord): Button = {
      Button(
        new SkullItemStackBuilder(record.playerName)
          .title(s"$YELLOW$BOLD${position}位:$WHITE${record.playerName}")
          .lore(
            s"$RESET${GREEN}総ログイン時間:${record.time.formatted}"
          )
          .build()
      )
    }

    ranking
      .recordsWithPositions
      .take(cutoff)
      .slice(pageIndex * perPage, pageIndex * perPage +perPage)
      .zipWithIndex
      .map { case ((position, record), index) =>
        index -> entry(position, record)
      }
  }

  private def totalLoginTimeSection(ranking: LoginTimeRanking): Seq[(Int, Button)] = {
    Seq(
      ChestSlotRef(5, 4) ->
        Button(
          new SkullItemStackBuilder(SkullOwners.unchama)
            .title(s"$YELLOW$UNDERLINE${BOLD}整地鯖統計データ")
            .lore(s"$RESET${AQUA}全プレイヤー総ログイン時間: ${ranking.totalLoginTime.formatted}")
            .build()
        )
    )
  }
  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = {
    for {
      ranking <- environment.loginTimeRankingApi.getRanking
    } yield {
      val records = ranking.recordsWithPositions
      val recordsToInclude = records.size min cutoff
      val totalNumberOfPages = Math.ceil(recordsToInclude / 45.0).toInt

      val combinedLayout =
        rankingSection(ranking)
          .++(uiOperationSection(totalNumberOfPages))
          .++(totalLoginTimeSection(ranking))

      MenuSlotLayout(combinedLayout: _*)
    }
  }
}
