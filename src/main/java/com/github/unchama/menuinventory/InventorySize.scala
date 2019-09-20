package com.github.unchama.menuinventory

/**
 * チェストインベントリの行数を表すcase class
 */
case class InventoryRowSize(val rows: Int)
object InventoryRowSize {
  /**
   * インベントリのサイズを表すデータ型.
   */
  type InventorySize = Either[InventoryRowSize, InventoryType]

  def Int.rows(): InventorySize = InventoryRowSize(this).left()
}
