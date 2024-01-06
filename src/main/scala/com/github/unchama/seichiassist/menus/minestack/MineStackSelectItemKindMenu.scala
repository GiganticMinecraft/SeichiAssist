package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import cats.implicits.toTraverseOps
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerReference}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectWithKindVariants
import eu.timepit.refined.auto._
import org.bukkit.ChatColor.{BOLD, DARK_BLUE}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.generic.MapExtra

object MineStackSelectItemKindMenu {

  class Environment(
    implicit val canOpenCategorizedMineStackMenu: CanOpen[IO, CategorizedMineStackMenu],
    implicit val canOpenSelectItemKindMenu: CanOpen[IO, MineStackSelectItemKindMenu],
    implicit val mineStackAPI: MineStackAPI[IO, Player, ItemStack],
    implicit val gachaPrizeAPI: GachaPrizeAPI[IO, ItemStack, Player]
  )

}

case class MineStackSelectItemKindMenu(
  group: MineStackObjectWithKindVariants[ItemStack],
  oldPage: Int,
  pageIndex: Int = 0
) extends Menu {

  import com.github.unchama.menuinventory.syntax._

  override type Environment = MineStackSelectItemKindMenu.Environment
  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_BLUE${BOLD}MineStack(アイテム種類選択)")

  /**
   * マインスタックオブジェクトボタンを置くセクションの行数
   */
  private val objectSectionRows = 5

  // ページ操作等のボタンを含むレイアウトセクション
  def uiOperationSection(
    totalNumberOfPages: Int
  )(page: Int)(implicit environment: Environment): Seq[(Int, Button)] = {
    import environment._

    def buttonToTransferTo(pageIndex: Int, skullOwnerReference: SkullOwnerReference): Button =
      CommonButtons.transferButton(
        new SkullItemStackBuilder(skullOwnerReference),
        s"MineStack(アイテム種類選択)${pageIndex + 1}ページ目へ",
        MineStackSelectItemKindMenu(group, oldPage, pageIndex)
      )

    val previousPageButtonSection =
      MapExtra.when(page > 0)(
        Map(ChestSlotRef(5, 7) -> buttonToTransferTo(page - 1, SkullOwners.MHF_ArrowUp))
      )

    val nextPageButtonSection =
      MapExtra.when(page + 1 < totalNumberOfPages)(
        Map(ChestSlotRef(5, 8) -> buttonToTransferTo(page + 1, SkullOwners.MHF_ArrowDown))
      )

    (previousPageButtonSection ++ nextPageButtonSection).toSeq
  }

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._
    val mineStackObjectPerPage = objectSectionRows.chestRows.slotCount

    val buttonMapping = (List(group.representative) ++ group.kindVariants)
      .slice(
        mineStackObjectPerPage * pageIndex,
        mineStackObjectPerPage * pageIndex + mineStackObjectPerPage
      )
      .zipWithIndex
      .map {
        case (inListMineStackObj, index) =>
          index -> MineStackButtons(player).getMineStackObjectButtonOf(inListMineStackObj)
      } ++ List(
      ChestSlotRef(5, 0) -> IO(
        CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp),
          s"MineStack${oldPage + 1}ページ目へ",
          CategorizedMineStackMenu(group.category, oldPage)
        )
      )
    )

    val totalNumberOfPages = Math.ceil((group.kindVariants.length + 1) / 45.0).toInt

    for {
      mapping <- buttonMapping.traverse(_.sequence)
    } yield MenuSlotLayout(mapping ++ uiOperationSection(totalNumberOfPages)(pageIndex): _*)

  }

}
