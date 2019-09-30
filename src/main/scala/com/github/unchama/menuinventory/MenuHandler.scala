package com.github.unchama.menuinventory

import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.entity.Player
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
    val whoClicked = event.getWhoClicked match {
      case player: Player => player
      case _ => return
    }

    //メニュー外のクリック排除
    val clickedInventory = event.getClickedInventory.ifNull { return }

    val holder = event.getWhoClicked.getOpenInventory.getTopInventory.getHolder match {
      case session: MenuSession => session
      case _ => return
    }

    //プレイヤーインベントリ内のクリック排除
    if (clickedInventory.getType == InventoryType.PLAYER) {
      event.setCancelled(true)
      return
    }

    val effect = holder.view.slotLayout.computeAsyncEffectOn(event)

    effect(whoClicked).unsafeRunAsync {
      case Left(error) =>
        println("Caught exception while handling a menu effect.")
        error.printStackTrace()
    }
  }
}
