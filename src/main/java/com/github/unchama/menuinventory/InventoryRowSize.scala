package com.github.unchama.menuinventory

import org.bukkit.event.inventory.InventoryType
/**
 * チェストインベントリの行数を表すcase class
 */
case class InventoryRowSize(val rows: Int)
object InventoryRowSize {
  /**
   * インベントリのサイズを表すデータ型.
   */
  type InventorySize = Either[InventoryRowSize, InventoryType]

  implicit class IntOps(val rowNumber: Int) extends AnyVal {
    def rows(): InventorySize = Left(InventoryRowSize(rowNumber))
  }
}
