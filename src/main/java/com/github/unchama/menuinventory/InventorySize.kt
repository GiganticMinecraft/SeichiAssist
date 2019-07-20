package com.github.unchama.menuinventory

import arrow.core.Either
import org.bukkit.event.inventory.InventoryType

/**
 * インベントリのサイズを表すデータ型.
 * [Either.Left] にある値はインベントリのスロット数を表し, [Either.Right] にある値は [InventoryType] のいずれかとなる.
 */
typealias InventorySize = Either<Int, InventoryType>
