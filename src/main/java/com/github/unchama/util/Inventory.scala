package com.github.unchama.util

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
