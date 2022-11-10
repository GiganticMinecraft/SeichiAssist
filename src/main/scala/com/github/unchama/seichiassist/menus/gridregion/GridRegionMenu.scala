package com.github.unchama.seichiassist.menus.gridregion

import cats.effect.IO
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.ChatColor._

object GridRegionMenu extends Menu {

  class Environment()

  override val frame: MenuFrame =
    MenuFrame(Right(InventoryType.DISPENSER), s"${LIGHT_PURPLE}グリッド式保護設定メニュー")

  override def computeMenuLayout(player: Player)(
    implicit environment: Environment
  ): IO[MenuSlotLayout] = ???

  case class computeButtons() {

    val toggleUnitPerClick

  }

}
