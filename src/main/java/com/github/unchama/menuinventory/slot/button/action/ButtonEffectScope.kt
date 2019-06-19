package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuSession
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect.UnfocusedEffect
import com.github.unchama.targetedeffect.unfocusedEffect
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * [ButtonEffect]に渡すScoped Lambdaの中で実行可能であるべきメソッドを提供するスコープオブジェクトのクラス.
 */
data class ButtonEffectScope(val event: InventoryClickEvent) {
  fun overwriteCurrentViewBy(newLayout: IndexedSlotLayout): UnfocusedEffect = unfocusedEffect {
    (event.inventory.holder as MenuSession).overwriteViewWith(newLayout)
  }

  fun overwriteCurrentSlotBy(newSlot: Slot): UnfocusedEffect = unfocusedEffect {
    val session = event.inventory.holder as MenuSession
    val newLayout = session.view.slotLayout.altered(event.slot to newSlot)
    session.overwriteViewWith(layout = newLayout)
  }
}