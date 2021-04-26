package com.github.unchama.seichiassist.menus.ranking

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.buildranking.domain.{BuildRanking, BuildRankingRecord}
import com.github.unchama.seichiassist.subsystems.ranking.RankingApi
import eu.timepit.refined.auto._
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object BuildRankingMenu {
  class Environment(
                     implicit val buildRankingApi: RankingApi[IO, BuildRanking],
                     val ioCanOpenBuildCountRankingMenu: IO CanOpen BuildRankingMenu,
                     val ioCanOpenFirstPage: IO CanOpen FirstPage.type
                   )
}

case class BuildRankingMenu(pageIndex: Int) extends Menu {
  final private val perPage = 45
  final private val cutoff = 150

  override type Environment = BuildRankingMenu.Environment
  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}建築神ランキング")

  private def uiOperationSection(totalNumberOfPages: Int)
                                (implicit environment: Environment): Seq[(Int, Button)] = {
    import environment._

    def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(skullOwnerReference),
        s"建築神ランキング${pageIndex + 1}ページ目へ",
        BuildRankingMenu(pageIndex)
      )

    val goBackToStickMenuSection =
      Seq(ChestSlotRef(5, 0) -> CommonButtons.openStickMenu)

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

  private def rankingSection(ranking: BuildRanking): Seq[(Int, Button)] = {
    def entry(position: Int, record: BuildRankingRecord): Button = {
      Button(
        new SkullItemStackBuilder(record.playerName)
          .title(s"$YELLOW$BOLD${position}位:$WHITE${record.playerName}")
          .lore(
            s"$RESET${GREEN}建築Lv:${record.buildAmountData.levelCorrespondingToExp.level}",
            s"$RESET${GREEN}総建築量:${record.buildAmountData.expAmount.amount.bigDecimal}"
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

  private def totalBuildAmountSection(ranking: BuildRanking): Seq[(Int, Button)] = {
    Seq(
      ChestSlotRef(5, 4) ->
        Button(
          new SkullItemStackBuilder(SkullOwners.unchama)
            .title(s"$YELLOW$UNDERLINE${BOLD}整地鯖統計データ")
            .lore(s"$RESET${AQUA}全プレイヤー総建築量: ${ranking.totalBuildExp}")
            .build()
        )
    )
  }

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(implicit environment: BuildRankingMenu.Environment): IO[MenuSlotLayout] = {
    for {
      ranking <- environment.buildRankingApi.getRanking
    } yield {
      val records = ranking.recordsWithPositions
      val recordsToInclude = records.size min cutoff
      val totalNumberOfPages = Math.ceil(recordsToInclude / 45.0).toInt

      val combinedLayout =
        rankingSection(ranking)
          .++(uiOperationSection(totalNumberOfPages))
          .++(totalBuildAmountSection(ranking))

      MenuSlotLayout(combinedLayout: _*)
    }
  }
}

