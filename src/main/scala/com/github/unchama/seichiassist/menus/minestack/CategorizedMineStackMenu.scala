package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.seichiassist.{MineStackObjectList, SkullOwners}
import com.github.unchama.targetedeffect.{DeferredEffect, TargetedEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

object CategorizedMineStackMenu {

  class Environment(implicit
                    val ioCanOpenMineStackMainMenu: IO CanOpen MineStackMainMenu.type,
                    val ioCanOpenCategorizedMenu: IO CanOpen CategorizedMineStackMenu,
                    val syncShift: MinecraftServerThreadShift[IO])

}

/**
 * カテゴリ別マインスタックメニューで [pageIndex] + 1 ページ目の[Menu]
 */
case class CategorizedMineStackMenu(category: MineStackObjectCategory, pageIndex: Int = 0) extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import eu.timepit.refined.auto._

  /**
   * マインスタックオブジェクトボタンを置くセクションの行数
   */
  val objectSectionRows = 5

  override type Environment = CategorizedMineStackMenu.Environment

  override val frame: MenuFrame = MenuFrame((objectSectionRows + 1).chestRows, s"$DARK_BLUE${BOLD}MineStack(${category.uiLabel})")

  def mineStackMainMenuButtonSection(implicit ioCanOpenMineStackMainMenu: IO CanOpen MineStackMainMenu.type): Seq[(Int, Button)] =
    Seq(
      ChestSlotRef(5, 0) -> CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
        "MineStackメインメニューへ",
        MineStackMainMenu
      )
    )

  // ページ操作等のボタンを含むレイアウトセクション
  def uiOperationSection(totalNumberOfPages: Int)
                        (category: MineStackObjectCategory, page: Int)
                        (implicit environment: Environment): Seq[(Int, Button)] = {
    import environment._

    def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(skullOwnerReference),
        s"MineStack${pageIndex + 1}ページ目へ",
        CategorizedMineStackMenu(category, pageIndex)
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

  override def open(implicit environment: CategorizedMineStackMenu.Environment,
                    ctx: LayoutPreparationContext,
                    syncCtx: MinecraftServerThreadShift[IO]): TargetedEffect[Player] = DeferredEffect {
    import MineStackObjectCategory._

    for {
      categoryItemList <- IO {
        MineStackObjectList.minestacklist.filter(_.category() == category)
      }
    } yield {
      val totalNumberOfPages = Math.ceil(categoryItemList.size / 45.0).toInt

      // オブジェクトリストが更新されるなどの理由でpageが最大値を超えてしまった場合、
      // 最後のページをopenする作用を返す
      if (pageIndex >= totalNumberOfPages)
        CategorizedMineStackMenu(category, totalNumberOfPages - 1).open
      else super.open
    }
  }

  override def computeMenuLayout(player: Player)
                                (implicit environment: Environment): IO[MenuSlotLayout] = {
    import MineStackObjectCategory._
    import cats.implicits._
    import environment._

    val mineStackObjectPerPage = objectSectionRows.chestRows.slotCount

    // TODO MineStackObjectListが可変になったらここを変更する
    val categoryItemList = MineStackObjectList.minestacklist.filter(_.category() == category)
    val totalNumberOfPages = Math.ceil(categoryItemList.size / 45.0).toInt

    val playerMineStackButtons = MineStackButtons(player)
    import playerMineStackButtons._

    // カテゴリ内のMineStackアイテム取り出しボタンを含むセクションの計算
    val categorizedItemSectionComputation =
      categoryItemList
        .slice(mineStackObjectPerPage * pageIndex, mineStackObjectPerPage * pageIndex + mineStackObjectPerPage).toList
        .traverse(getMineStackItemButtonOf(_))
        .map(_.zipWithIndex.map(_.swap))

    // 自動スタック機能トグルボタンを含むセクションの計算
    val autoMineStackToggleButtonSectionComputation =
      List(ChestSlotRef(5, 4) -> computeAutoMineStackToggleButton)
        .map(_.sequence)
        .sequence

    for {
      categorizedItemSection <- categorizedItemSectionComputation
      autoMineStackToggleButtonSection <- autoMineStackToggleButtonSectionComputation
    } yield {
      val combinedLayout =
        uiOperationSection(totalNumberOfPages)(category, pageIndex)
          .++(categorizedItemSection)
          .++(autoMineStackToggleButtonSection)

      MenuSlotLayout(combinedLayout: _*)
    }
  }
}
