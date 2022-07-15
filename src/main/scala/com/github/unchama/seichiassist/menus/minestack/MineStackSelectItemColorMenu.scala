package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import cats.implicits.toTraverseOps
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.{
  MineStackObjectWithColorVariants,
  MineStackObject
}
import eu.timepit.refined.auto._
import org.bukkit.ChatColor.{BOLD, DARK_BLUE}
import org.bukkit.entity.Player

object MineStackSelectItemColorMenu {

  class Environment(
    implicit val canOpenCategorizedMineStackMenu: CanOpen[IO, CategorizedMineStackMenu]
  )

}

case class MineStackSelectItemColorMenu(group: MineStackObjectWithColorVariants) extends Menu {

  import com.github.unchama.menuinventory.syntax._

  override type Environment = MineStackSelectItemColorMenu.Environment
  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_BLUE${BOLD}MineStack(アイテム色選択)")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: MineStackSelectItemColorMenu.Environment): IO[MenuSlotLayout] = {
    import environment.canOpenCategorizedMineStackMenu
    val buttonMapping = (List(group.representative) ++ group.coloredVariants).zipWithIndex.map {
      case (inListMineStackObj, index) =>
        val mineStackObjectGroup: Either[MineStackObject, MineStackObjectWithColorVariants] =
          Left(inListMineStackObj)
        index -> MineStackButtons(player).getMineStackObjectButtonOf(inListMineStackObj)
    } ++ List(
      ChestSlotRef(5, 0) -> IO(
        CommonButtons.transferButton(
          new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp),
          s"MineStack1ページ目へ",
          CategorizedMineStackMenu(group.category)
        )
      )
    )
    for {
      mapping <- buttonMapping.traverse(_.sequence)
    } yield MenuSlotLayout(mapping: _*)

  }

}
