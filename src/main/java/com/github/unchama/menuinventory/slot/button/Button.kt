package com.github.unchama.menuinventory.slot.button

import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.slot.button.action.ButtonAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * メニューインベントリ上で「ボタン」として動作する[Slot]のクラス.
 *
 * [actionList]により与えられる作用をリストの順で一つづつ実行していきます.
 *
 * [runEffect]は常に与えられた[InventoryClickEvent]をキャンセルします.
 *
 * @param itemStack  [Inventory] へセットする [ItemStack]
 * @author karayuu
 */
class Button(override val itemStack: ItemStack,
             private val actionList: List<ButtonAction>) : Slot {

  /**
   * [actions]をひとつずつ作用として発生させる [Slot] を構築します.
   */
  constructor(itemStack: ItemStack, vararg actions: ButtonAction): this(itemStack, actions.toList())

  override fun runEffect(event: InventoryClickEvent) {
    event.isCancelled = true
    this.actionList.forEach { it.invoke(event) }
  }

}
