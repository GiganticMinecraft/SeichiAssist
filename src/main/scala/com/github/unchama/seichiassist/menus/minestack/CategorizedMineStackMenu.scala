package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.seichiassist.{MineStackObjectList, SkullOwners}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object CategorizedMineStackMenu {

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}
  import eu.timepit.refined.auto._

  private val mineStackObjectPerPage = 5.chestRows.slotCount

  /**
   * カテゴリ別マインスタックメニューで [pageIndex] + 1 ページ目の[Menu]
   */
  def forCategory(category: MineStackObjectCategory, pageIndex: Int = 0): Menu = new Menu {
    override val frame: MenuFrame =
      MenuFrame(6.chestRows, s"$DARK_BLUE${BOLD}MineStack(${category.uiLabel})")

    override def computeMenuLayout(player: Player): IO[MenuSlotLayout] =
      computeMenuLayoutOn(category, pageIndex)(player)
  }

  private def computeMenuLayoutOn(category: MineStackObjectCategory, page: Int)(player: Player): IO[MenuSlotLayout] = {
    import MineStackObjectCategory._
    import cats.implicits._

    val categoryItemList = MineStackObjectList.minestacklist.filter(_.category() == category)
    val totalNumberOfPages = Math.ceil(categoryItemList.size / 45.0).toInt

    // オブジェクトリストが更新されるなどの理由でpageが最大値を超えてしまった場合、最後のページを計算する
    if (page >= totalNumberOfPages) return computeMenuLayoutOn(category, totalNumberOfPages - 1)(player)

    val playerMineStackButtons = MineStackButtons(player)
    import playerMineStackButtons._

    val uiOperationSection = Sections.uiOperationSection(totalNumberOfPages)(category, page)

    // カテゴリ内のMineStackアイテム取り出しボタンを含むセクションの計算
    val categorizedItemSectionComputation =
      categoryItemList
        .slice(mineStackObjectPerPage * page, mineStackObjectPerPage * page + mineStackObjectPerPage).toList
        .traverse(getMineStackItemButtonOf)
        .map(_.zipWithIndex.map(_.swap))

    // 自動スタック機能トグルボタンを含むセクションの計算
    val autoMineStackToggleButtonSectionComputation =
      List(ChestSlotRef(5, 4) -> computeAutoMineStackToggleButton())
        .map(_.sequence)
        .sequence

    for {
      categorizedItemSection <- categorizedItemSectionComputation
      autoMineStackToggleButtonSection <- autoMineStackToggleButtonSectionComputation
      combinedLayout = uiOperationSection.++(categorizedItemSection).++(autoMineStackToggleButtonSection)
    } yield MenuSlotLayout(combinedLayout: _*)
  }

  object Sections {
    val mineStackMainMenuButtonSection: Seq[(Int, Button)] = Seq(
      ChestSlotRef(5, 0) -> CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
        "MineStackメインメニューへ",
        MineStackMainMenu
      )
    )

    // ページ操作等のボタンを含むレイアウトセクション
    def uiOperationSection(totalNumberOfPages: Int)(category: MineStackObjectCategory, page: Int): Seq[(Int, Button)] = {
      def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
        CommonButtons.transferButton(
          new SkullItemStackBuilder(skullOwnerReference),
          s"MineStack${pageIndex + 1}ページ目へ",
          forCategory(category, pageIndex)
        )

      val previousPageButtonSection =
        if (page > 0)
          Seq(ChestSlotRef(5, 7) -> buttonToTransferTo(page - 1, SkullOwners.MHF_ArrowUp))
        else
          Seq()

      val nextPageButtonSection =
        if (page + 1 < totalNumberOfPages)
          Seq(ChestSlotRef(5, 8) -> buttonToTransferTo(page + 1, SkullOwners.MHF_ArrowDown))
        else
          Seq()

      mineStackMainMenuButtonSection ++ previousPageButtonSection ++ nextPageButtonSection
    }
  }
}
