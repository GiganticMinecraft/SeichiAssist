package com.github.unchama.seichiassist.menus.nicknames

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.ChatColor._

object NickNameMenu extends Menu {

  override type Environment = this.type

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}二つ名組み合わせシステム")

  override def computeMenuLayout(player: Player)(
    implicit environment: Environment
  ): IO[MenuSlotLayout] = ???

}
