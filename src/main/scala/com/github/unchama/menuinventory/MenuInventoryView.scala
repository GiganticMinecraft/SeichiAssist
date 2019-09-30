package com.github.unchama.menuinventory

import com.github.unchama.menuinventory.InventoryRowSize.InventorySize
import com.github.unchama.util.InventoryUtil._
import org.bukkit.inventory.{Inventory, InventoryHolder}

/**
 * 入っているアイテムスタックをクリックすることで作用が引き起こされるような
 * インベントリのイミュータブルなビューを表すオブジェクトのクラス.
 *
 * @param size インベントリのサイズを決定するデータ
 * @param title インベントリのタイトル
 * @param slotLayout インベントリの各スロットのindexと[Slot]を対応付ける[IndexedSlotLayout]
 */
case class MenuInventoryView(size: InventorySize,
                             title: String,
                             slotLayout: IndexedSlotLayout = IndexedSlotLayout()) {
  private[menuinventory] def createConfiguredInventory(holder: InventoryHolder): Inventory = {
    val inventory = createInventory(Some(holder), size, Some(title))
    slotLayout.setItemsOn(inventory)
    inventory
  }

  def createNewSession(): MenuSession = new MenuSession(this)
}
