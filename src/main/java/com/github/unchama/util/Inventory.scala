package com.github.unchama.util

import com.github.unchama.menuinventory.InventoryRowSize.InventorySize
import org.bukkit.Bukkit
import org.bukkit.inventory.{Inventory, InventoryHolder}
object InventoryUtil {
  /**
   * 主にチェスト用。
   */
  val Inventory.row: Int
    get() = size / 9

  def createInventory(holder: InventoryHolder? = null,
                      size: InventorySize = 4.rows(),
                      title: String? = null): Inventory =
    when (size) {
      is Either.Left => Bukkit.createInventory(holder, size.a.rows * 9, title)
        is Either.Right => Bukkit.createInventory(holder, size.b, title)
    }
}
