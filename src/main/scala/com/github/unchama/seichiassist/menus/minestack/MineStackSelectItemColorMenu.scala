package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.minestack.MineStackObj
import org.bukkit.ChatColor.{BOLD, DARK_BLUE}
import org.bukkit.entity.Player

object MineStackSelectItemColorMenu {

  class Environment(implicit val mineStackSelectItemColorMenu: MineStackSelectItemColorMenu)

}

case class MineStackSelectItemColorMenu(mineStackObj: MineStackObj) extends Menu {

  import com.github.unchama.menuinventory.syntax._

  override type Environment = MineStackSelectItemColorMenu.Environment
  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_BLUE${BOLD}MineStack(アイテム色選択)")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: MineStackSelectItemColorMenu.Environment): IO[MenuSlotLayout] = {
    val buttonMapping = (
      0 -> MineStackButtons(player).getMineStackItemButtonOf(mineStackObj).unsafeRunSync()
    ) :: MineStackObjectList.minestacklisttoggle(mineStackObj).zipWithIndex.map {
      case (inListMineStackObj, index) =>
        (index + 1) -> MineStackButtons(player)
          .getMineStackItemButtonOf(inListMineStackObj)
          .unsafeRunSync()
    }
    IO(MenuSlotLayout(buttonMapping: _*))
  }
}
