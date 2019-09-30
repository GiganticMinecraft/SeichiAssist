package com.github.unchama.menuinventory.slot.button.action

import cats.effect.IO
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.{IndexedSlotLayout, MenuSession}
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * [ButtonEffect]に渡すScoped Lambdaの中で実行可能であるべきメソッドを提供するスコープオブジェクトのクラス.
 */
case class ButtonEffectScope(event: InventoryClickEvent) {
  def overwriteCurrentViewBy(newLayout: IndexedSlotLayout): IO[Unit] = IO {
    event.getInventory.getHolder.asInstanceOf[MenuSession].overwriteViewWith(newLayout)
  }

  def overwriteCurrentSlotBy(newSlot: Slot): IO[Unit] = IO {
    val session = event.getInventory.getHolder.asInstanceOf[MenuSession]
    val newLayout = session.view.slotLayout.altered(event.getSlot -> newSlot)
    session.overwriteViewWith(layout = newLayout)
  }
}