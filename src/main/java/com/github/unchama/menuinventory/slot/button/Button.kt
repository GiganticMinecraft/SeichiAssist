package com.github.unchama.menuinventory.slot.button

import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.slot.button.action.ButtonAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * メニューインベントリ上で「ボタン」として動作する[Slot]のクラス.
 * [runEffect]は常に与えられた[InventoryClickEvent]をキャンセルします.
 *
 * @param itemStack  [Inventory] へセットする [ItemStack]
 * @author karayuu
 */
class Button(override val itemStack: ItemStack,
             private val actionList: List<ButtonAction>) : Slot {

  override fun runEffect(event: InventoryClickEvent) {
    event.isCancelled = true
    this.actionList.forEach { it.invoke(event) }
  }

}
