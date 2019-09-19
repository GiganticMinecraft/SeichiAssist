package com.github.unchama.menuinventory

import arrow.core.Either
import arrow.effects.extensions.io.fx.fx
import arrow.effects.extensions.io.unsafeRun.runNonBlocking
import arrow.unsafe
import org.bukkit.entity.Player
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
    val whoClicked = event.whoClicked as? Player ?: return

    //メニュー外のクリック排除
    val clickedInventory = event.clickedInventory ?: return
    val openInventory = event.whoClicked.openInventory.topInventory

    //プレイヤーインベントリ内のクリック排除
    if (openInventory.holder is MenuSession && clickedInventory.type === InventoryType.PLAYER) {
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
