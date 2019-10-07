package com.github.unchama.menuinventory

import cats.effect.IO
import com.github.unchama.menuinventory.InventoryRowSize.InventorySize
import com.github.unchama.util.InventoryUtil._
import org.bukkit.inventory.{Inventory, InventoryHolder}

/**
 * @param size インベントリのサイズを決定するデータ
 * @param title インベントリのタイトル
 */
case class InventoryFrame(size: InventorySize, title: String) {
  private[menuinventory] def createConfiguredInventory(holder: InventoryHolder): Inventory =
    createInventory(Some(holder), size, Some(title))

  def createNewSession(): IO[MenuSession] = IO(new MenuSession(this))
}
