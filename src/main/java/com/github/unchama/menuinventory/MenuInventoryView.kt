package com.github.unchama.menuinventory

import arrow.core.Either
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.seichiassist.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * インベントリのサイズを表すデータ型.
 * [Either.Left] にある値はインベントリのスロット数を表し, [Either.Right] にある値は [InventoryType] のいずれかとなる.
 */
typealias InventorySize = Either<Int, InventoryType>

/**
 * 入っているアイテムスタックをクリックすることで作用が引き起こされるような
 * インベントリのイミュータブルなビューを表すオブジェクトのクラス.
 *
 * @param size インベントリのサイズを決定するデータ
 * @param title インベントリのタイトル
 * @param slotLayout インベントリの各スロットのindexと[Slot]を対応付ける[IndexedSlotLayout]
 */
data class MenuInventoryView(private val size: InventorySize,
                             private val title: String,
                             internal val slotLayout: IndexedSlotLayout) {
  internal fun getConfiguredInventory(holder: InventoryHolder): Inventory {
    fun createInventory(property: InventorySize, title: String): Inventory =
        when (property) {
          is Either.Left -> Bukkit.createInventory(holder, property.a, title)
          is Either.Right -> Bukkit.createInventory(holder, property.b, title)
        }

    return runBlocking {
      createInventory(size, title).also {
        slotLayout.asynchronouslySetItemsOn(it)
      }
    }

  }
}
