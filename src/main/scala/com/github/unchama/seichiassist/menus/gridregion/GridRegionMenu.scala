package com.github.unchama.seichiassist.menus.gridregion

import cats.effect.IO
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.ChatColor._

object GridRegionMenu extends Menu {

  class Environment()

  /**
   * メニューのサイズとタイトルに関する情報
   */
  override val frame: MenuFrame =
    MenuFrame(Right(InventoryType.DISPENSER), s"${LIGHT_PURPLE}グリッド式保護設定メニュー")

  /**
   * @return
   * `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def computeMenuLayout(player: Player)(
    implicit environment: Environment
  ): IO[MenuSlotLayout] = ???
}
