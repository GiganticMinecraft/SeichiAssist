package com.github.unchama.seichiassist.menus.ranking

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.ranking.RankingApi
import com.github.unchama.seichiassist.subsystems.ranking.domain.{SeichiRanking, SeichiRankingRecord}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object SeichiRankingMenu {

  class Environment(implicit
                    val seichiRankingApi: RankingApi[IO],
                    val ioCanOpenSeichiRankingMenu: IO CanOpen SeichiRankingMenu,
                    val ioCanOpenStickMenu: IO CanOpen FirstPage.type)

}

case class SeichiRankingMenu(pageIndex: Int) extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import eu.timepit.refined.auto._

  final private val displayPerPage = 45

  // 表示するランクの下限
  final private val rankCutoff = 150

  override type Environment = SeichiRankingMenu.Environment

  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}整地神ランキング")

  private def uiOperationSection(totalNumberOfPages: Int)
                                (implicit environment: Environment): Seq[(Int, Button)] = {
    import environment._

    def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(skullOwnerReference),
        s"整地神ランキング${pageIndex + 1}ページ目へ",
        SeichiRankingMenu(pageIndex)
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

  private def totalBreakAmountSection(ranking: SeichiRanking): Seq[(Int, Button)] = {
    Seq(
      ChestSlotRef(5, 4) ->
        Button(
          new SkullItemStackBuilder(SkullOwners.unchama)
            .title(s"$YELLOW$UNDERLINE${BOLD}整地鯖統計データ")
            .lore(s"$RESET${AQUA}全プレイヤー総整地量: ${ranking.totalBreakAmount.amount}")
            .build()
        )
    )
  }

  private def rankingSection(ranking: SeichiRanking): Seq[(Int, Button)] = {
    def entry(position: Int, record: SeichiRankingRecord): Button = {
      val level = record.seichiAmountData.levelCorrespondingToExp.level
      val displayLevel = if (level == 200) {
        s"$RESET${GREEN}整地Lv:$level☆${record.seichiAmountData.starLevelCorrespondingToExp}"
      } else {
        s"$RESET${GREEN}整地Lv:$level"
      }
      Button(
        new SkullItemStackBuilder(record.playerName)
          .title(s"$YELLOW$BOLD${position}位:$WHITE${record.playerName}")
          .lore(
            s"$RESET${GREEN}整地Lv:$displayLevel",
            s"$RESET${GREEN}総整地量:${record.seichiAmountData.expAmount.amount}"
          )
          .build()
      )
    }

    ranking
      .recordsWithPositions
      .take(rankCutoff)
      .slice(pageIndex * displayPerPage, pageIndex * displayPerPage + displayPerPage)
      .zipWithIndex
      .map { case ((position, record), index) =>
        index -> entry(position, record)
      }
  }

  override def computeMenuLayout(player: Player)
                                (implicit environment: Environment): IO[MenuSlotLayout] = {
    for {
      ranking <- environment.seichiRankingApi.getSeichiRanking
    } yield {
      val records = ranking.recordsWithPositions
      val recordsToInclude = records.size min rankCutoff
      val totalNumberOfPages = Math.ceil(recordsToInclude / 45.0).toInt

      val combinedLayout =
        rankingSection(ranking)
          .++(uiOperationSection(totalNumberOfPages))
          .++(totalBreakAmountSection(ranking))

      MenuSlotLayout(combinedLayout: _*)
    }
  }
}
