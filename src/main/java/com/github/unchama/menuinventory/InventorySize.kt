package com.github.unchama.menuinventory

import arrow.core.Either
import arrow.core.left
import org.bukkit.event.inventory.InventoryType

/**
 * チェストインベントリの行数を表すdata class
 */
data class InventoryRowSize(val rows: Int)

/**
 * インベントリのサイズを表すデータ型.
 */
typealias InventorySize = Either<InventoryRowSize, InventoryType>

fun Int.rows(): InventorySize = InventoryRowSize(this).left()
