package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryDragEvent, InventoryInteractEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{AnvilInventory, ItemStack}

class MebiusRenamePreventionListener extends Listener {

  private def isMebius(stack: ItemStack) = ItemStackMebiusCodec.decodeMebiusProperty(stack).nonEmpty

  private def cancelEventAndNotifyTheAnternative(event: InventoryInteractEvent): Unit = {
    event.setCancelled(true)
    event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
  }

  // 金床配置時（クリック）
  @EventHandler def onRenameOnAnvil(event: InventoryClickEvent): Unit = {
    // 金床を開いていない場合終了
    if (!event.getView.getTopInventory.isInstanceOf[AnvilInventory]) return

    val clickedInventory = event.getClickedInventory
    if (clickedInventory.isInstanceOf[AnvilInventory]) {
      // mebiusを選択中、左枠に置いた場合はcancel
      if (isMebius(event.getCursor) && event.getView.convertSlot(0) == 0 && event.getRawSlot == 0) {
        cancelEventAndNotifyTheAnternative(event)
      }
    } else if (event.getClick.isShiftClick && isMebius(event.getCurrentItem)) {
      // mebiusをShiftクリックした場合
      // 金床の左枠が空いている場合はcancel
      if (event.getView.getTopInventory.getItem(0) == null) {
        cancelEventAndNotifyTheAnternative(event)
      }
    }
  }

  // 金床配置時（ドラッグ）
  @EventHandler def onDragInAnvil(event: InventoryDragEvent): Unit = {
    // 金床じゃなければreturn
    if (!event.getInventory.isInstanceOf[AnvilInventory]) return

    // mebiusを選択中じゃなければreturn
    if (!isMebius(event.getOldCursor)) return

    if (event.getRawSlots.contains(0) && event.getView.convertSlot(0) == 0) {
      cancelEventAndNotifyTheAnternative(event)
    }
  }
}
