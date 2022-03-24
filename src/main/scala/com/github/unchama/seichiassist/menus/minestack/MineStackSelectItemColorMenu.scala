package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.{MineStackObjectList, SkullOwners}
import eu.timepit.refined.auto._
import org.bukkit.ChatColor.{BOLD, DARK_BLUE}
import org.bukkit.entity.Player

object MineStackSelectItemColorMenu {

  class Environment(
    implicit val canOpenCategorizedMineStackMenu: CanOpen[IO, CategorizedMineStackMenu]
  )

}

case class MineStackSelectItemColorMenu(mineStackObj: MineStackObj) extends Menu {

  import com.github.unchama.menuinventory.syntax._

  override type Environment = MineStackSelectItemColorMenu.Environment
  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_BLUE${BOLD}MineStack(アイテム色選択)")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: MineStackSelectItemColorMenu.Environment): IO[MenuSlotLayout] = {
    import environment.canOpenCategorizedMineStackMenu
    val buttonMapping = (
      0 -> MineStackButtons(player).getMineStackItemButtonOf(mineStackObj).unsafeRunSync()
    ) :: MineStackObjectList.minestacklisttoggle(mineStackObj).zipWithIndex.map {
      case (inListMineStackObj, index) =>
        (index + 1) -> MineStackButtons(player)
          .getMineStackItemButtonOf(inListMineStackObj)
          .unsafeRunSync()
    } ++ Seq(
      ChestSlotRef(5, 0) -> CommonButtons.transferButton(
        new SkullItemStackBuilder(SkullOwners.MHF_ArrowUp),
        s"MineStack1ページ目へ",
        CategorizedMineStackMenu(mineStackObj.stackType, 1)
      )
    )

    IO {
      MenuSlotLayout(buttonMapping: _*)
    }
  }

}
