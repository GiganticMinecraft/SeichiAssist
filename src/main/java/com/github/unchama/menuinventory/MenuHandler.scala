package com.github.unchama.menuinventory

import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
/**
 * [MenuInventoryView] に由来するインベントリ上のクリックイベントをビューに定義されたアクションに流すようなリスナーオブジェクト.
 *
 * @author karayuu
 */
object MenuHandler extends Listener {
  @EventHandler
  def onInventoryClick(event: InventoryClickEvent) {
    val whoClicked = event.whoClicked as? Player ?: return

    //メニュー外のクリック排除
    val clickedInventory = event.clickedInventory ?: return
    val openInventory = event.whoClicked.openInventory.topInventory

    //プレイヤーインベントリ内のクリック排除
    if (openInventory.holder is MenuSession && clickedInventory.getType === InventoryType.PLAYER) {
      event.isCancelled = true
      return
    }

    val holder = clickedInventory.holder

    if (holder is MenuSession) {
      val effect = holder.view.slotLayout.computeAsyncEffectOn(event)

      unsafe {
        runNonBlocking({
          fx {
            !effect {
              effect.runFor(whoClicked)
            }
          }
        }) { if (it is Either.Left) it.a.printStackTrace() }
      }
    }
  }
}
