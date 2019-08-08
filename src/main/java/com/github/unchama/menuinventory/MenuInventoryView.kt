package com.github.unchama.menuinventory

import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.util.createInventory
import kotlinx.coroutines.runBlocking
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
    return runBlocking {
      createInventory(holder, size, title).also {
        slotLayout.asynchronouslySetItemsOn(it)
      }
    }
  }

  fun createNewSession(): MenuSession = MenuSession(this)
}
