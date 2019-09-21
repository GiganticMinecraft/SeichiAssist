package com.github.unchama.util

import com.github.unchama.menuinventory.InventoryRowSize.InventorySize
import org.bukkit.Bukkit
import org.bukkit.inventory.{Inventory, InventoryHolder}
object InventoryUtil {
  import com.github.unchama.menuinventory.InventoryRowSize.IntToInventorySize
  /**
   * 主にチェスト用。
   */
  implicit class InventoryExtension(val inventory: Inventory) {
    def row: Int = inventory.getSize / 9
  }

  def createInventory(holder: InventoryHolder = null,
                      size: InventorySize = 4.rows,
                      title: String = null): Inventory =
    size match {
      case Left(a) => Bukkit.createInventory(holder, a.rows * 9, title)
      case Right(b) => Bukkit.createInventory(holder, b, title)
    }
}
