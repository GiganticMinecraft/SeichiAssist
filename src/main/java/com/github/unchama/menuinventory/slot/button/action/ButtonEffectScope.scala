package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect.UnfocusedEffect
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * [ButtonEffect]に渡すScoped Lambdaの中で実行可能であるべきメソッドを提供するスコープオブジェクトのクラス.
 */
case class ButtonEffectScope(val event: InventoryClickEvent) {
  def overwriteCurrentViewBy(newLayout: IndexedSlotLayout): UnfocusedEffect = UnfocusedEffect {
    (event.inventory.holder.asInstanceOf[MenuSession]).overwriteViewWith(newLayout)
  }

  def overwriteCurrentSlotBy(newSlot: Slot): UnfocusedEffect = UnfocusedEffect {
    val session = event.inventory.holder.asInstanceOf[MenuSession]
    val newLayout = session.view.slotLayout.altered(event.slot to newSlot)
    session.overwriteViewWith(layout = newLayout)
  }
}