package com.github.unchama.menuinventory

import cats.effect.IO
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.{EventHandler, Listener}

/**
 * [MenuInventoryView] に由来するインベントリ上のクリックイベントをビューに定義されたアクションに流すようなリスナーオブジェクト.
 *
 * @author karayuu
 */
class MenuHandler(implicit val cs: NonServerThreadContextShift[IO], env: EffectEnvironment) extends Listener {
  @EventHandler(ignoreCancelled = true)
  def onInventoryClick(event: InventoryClickEvent): Unit = {
    val whoClicked = event.getWhoClicked match {
      case player: Player => player
      case _ => return
    }

    //メニュー外のクリック排除
    val clickedInventory = event.getClickedInventory.ifNull {
      return
    }

    val holder = event.getWhoClicked.getOpenInventory.getTopInventory.getHolder match {
      case session: MenuSession => session
      case _ => return
    }

    // 上インベントリ以外のクリックを排除
    if (clickedInventory != event.getWhoClicked.getOpenInventory.getTopInventory) {
      event.setCancelled(true)
      return
    }

    env.runEffectAsync(
      "メニューのクリックを非同期で処理する",
      for {
        currentLayout <- holder.currentLayout.get
        _ <- currentLayout.effectOn(event)(cs)(whoClicked)
      } yield ()
    )
  }
}
