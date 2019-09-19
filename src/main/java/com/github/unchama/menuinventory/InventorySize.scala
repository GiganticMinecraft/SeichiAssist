package com.github.unchama.menuinventory

/**
 * チェストインベントリの行数を表すcase class
 */
case class InventoryRowSize(val rows: Int)

/**
 * インベントリのサイズを表すデータ型.
 */
typealias InventorySize = Either<InventoryRowSize, InventoryType>

def Int.rows(): InventorySize = InventoryRowSize(this).left()
