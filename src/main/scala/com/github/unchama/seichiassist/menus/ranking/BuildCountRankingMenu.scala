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
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI
import com.github.unchama.seichiassist.subsystems.ranking.domain.{SeichiRanking, SeichiRankingRecord}
import eu.timepit.refined.auto._
import org.bukkit.entity.Player
import org.bukkit.ChatColor._

object BuildCountRankingMenu {
  class Environment(
                     implicit val buildCountAPI: BuildCountAPI[IO, Player],
                     val ioCanOpenBuildCountRankingMenu: IO CanOpen BuildCountRankingMenu,
                     val ioCanOpenFirstPage: IO CanOpen FirstPage.type
                   )
}

case class BuildCountRankingMenu(pageIndex: Int) extends Menu {
  final private val perPage = 45
  final private val cutoff = 150

  override type Environment = BuildCountRankingMenu.Environment
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
        BuildCountRankingMenu(pageIndex)
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

  // TODO 正しくない。BuildRankingとかを要求するべき
  private def rankingSection(ranking: SeichiRanking): Seq[(Int, Button)] = {
    def entry(position: Int, record: SeichiRankingRecord): Button = {
      Button(
        new SkullItemStackBuilder(record.playerName)
          .title(s"$YELLOW$BOLD${position}位:$WHITE${record.playerName}")
          .lore(
            s"$RESET${GREEN}建築Lv:${record.seichiAmountData.levelCorrespondingToExp.level}",
            s"$RESET${GREEN}総建築量:${record.seichiAmountData.expAmount.amount}"
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

  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(implicit environment: BuildCountRankingMenu.Environment): IO[MenuSlotLayout] = {
    val buildCountRepo = environment.buildCountAPI.playerBuildAmountRepository
    for {
      ranking <- ???
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

