package com.github.unchama.seichiassist.menus.ranking

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.menus.{CommonButtons, ranking}
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import org.bukkit.entity.Player
import org.bukkit.ChatColor._

object PlayTimeRankingMenu {
  class Environment(implicit val ioCanOpenPlayTimeRankingMenu: IO CanOpen PlayTimeRankingMenu,
                    val ioCanOpenMainMenu: IO CanOpen FirstPage.type)
}

case class PlayTimeRankingMenu(pageIndex: Int) extends Menu {
  final private val displayPerPage = 45
  final private val cutoff = 150

  override type Environment = PlayTimeRankingMenu.Environment

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}ログイン神ランキング")

  private def uiOperationSection(totalNumberOfPages: Int)
                                (implicit environment: Environment): Seq[(Int, Button)] = {
    import environment._
    import eu.timepit.refined.auto._

    def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(skullOwnerReference),
        s"ログイン神ランキング${pageIndex + 1}ページ目へ",
        PlayTimeRankingMenu(pageIndex)
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
  /**
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = IO {
    ??? // TODO
  }
}
