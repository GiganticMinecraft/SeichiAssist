package com.github.unchama.menuinventory

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

/**
 * [MenuInventoryView] に由来するインベントリ上のクリックイベントをビューに定義されたアクションに流すようなリスナーオブジェクト.
 *
 * @author karayuu
 */
object MenuHandler : Listener {
  @EventHandler
  fun onInventoryClick(event: InventoryClickEvent) {
    if (event.whoClicked.type != EntityType.PLAYER) return

    //メニュー外のクリック排除
    val clickedInventory = event.clickedInventory ?: return
    val openInventory = event.whoClicked.openInventory.topInventory

    //プレイヤーインベントリ内のクリック排除
    if (openInventory.holder is MenuInventoryView && clickedInventory.type == InventoryType.PLAYER) {
      event.isCancelled = true
      return
    }

    val holder = clickedInventory.holder

    if (holder is MenuInventoryView) {
      holder.getEffectTriggerAt(event.slot)(event)
    }
  }
}
