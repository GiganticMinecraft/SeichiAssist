package com.github.unchama.seichiassist.subsystems.mebius.bukkit.listeners

import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import org.bukkit.ChatColor._
import org.bukkit.event.inventory.{InventoryAction, InventoryClickEvent, InventoryDragEvent, InventoryInteractEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.AnvilInventory

class MebiusRenamePreventionListener extends Listener {

  // 金床配置時（クリック）
  @EventHandler def onRenameOnAnvil(event: InventoryClickEvent): Unit = {
    // 金床を開いていない場合終了
    if (!event.getView.getTopInventory.isInstanceOf[AnvilInventory]) return

    val clickedInventory = event.getClickedInventory
    if (clickedInventory.isInstanceOf[AnvilInventory]) {
      // mebiusを選択中、左枠に置いた場合はcancel
      if (BukkitMebiusItemStackCodec.isMebius(event.getCursor) && event.getView.convertSlot(0) == 0 && event.getRawSlot == 0) {
        cancelEventAndNotifyTheAlternative(event)
      }
    } else if (event.getClick.isShiftClick && BukkitMebiusItemStackCodec.isMebius(event.getCurrentItem)) {
      // mebiusをShiftクリックした場合
      // 金床の左枠が空いている場合はcancel
      if (event.getView.getTopInventory.getItem(0) == null) {
        cancelEventAndNotifyTheAlternative(event)
      }
    }
  }

  private def cancelEventAndNotifyTheAlternative(event: InventoryInteractEvent): Unit = {
    event.setCancelled(true)
    event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
  }

  // 金床配置時（ドラッグ）
  @EventHandler def onDragInAnvil(event: InventoryDragEvent): Unit = {
    // 金床じゃなければreturn
    if (!event.getInventory.isInstanceOf[AnvilInventory]) return

    // mebiusを選択中じゃなければreturn
    if (!BukkitMebiusItemStackCodec.isMebius(event.getOldCursor)) return

    if (event.getRawSlots.contains(0) && event.getView.convertSlot(0) == 0) {
      cancelEventAndNotifyTheAlternative(event)
    }
  }

  // 数字キーでホットバーのアイテムを金床のスロットに移動している時
  @EventHandler def onSwap(event: InventoryClickEvent): Unit = {
    // 金床を開いていない場合終了
    if (!event.getView.getTopInventory.isInstanceOf[AnvilInventory]) return

    val action = event.getAction
    // event.getActionが数字キーによるアイテムの移動ならば処理を続行
    if (action == null || (action != InventoryAction.HOTBAR_SWAP && action != InventoryAction.HOTBAR_MOVE_AND_READD)) return

    // 金床の一番左のスロットにアイテムを移動させた場合以外return
    if (event.getView.convertSlot(0) != 0 || event.getRawSlot != 0) return

    // Mebiusを移動させた場合
    val bottomInventory = event.getView.getBottomInventory
    val keyNum = event.getHotbarButton
    if (BukkitMebiusItemStackCodec.isMebius(bottomInventory.getItem(keyNum))) {
      cancelEventAndNotifyTheAlternative(event)
    }
  }
}
