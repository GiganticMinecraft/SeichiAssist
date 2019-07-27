package com.github.unchama.menuinventory

import arrow.core.Either
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.util.newChestInventory
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

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
                             internal val slotLayout: IndexedSlotLayout = IndexedSlotLayout()) {
  internal fun createConfiguredInventory(holder: InventoryHolder): Inventory {
    fun createInventory(property: InventorySize, title: String): Inventory =
        when (property) {
          is Either.Left -> newChestInventory(holder, property.a, title)
          is Either.Right -> Bukkit.createInventory(holder, property.b, title)
        }

    return runBlocking {
      createInventory(size, title).also {
        slotLayout.asynchronouslySetItemsOn(it)
      }
    }
  }

  fun createNewSession(): MenuSession = MenuSession(this)
}
