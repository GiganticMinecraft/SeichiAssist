package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuSession
import com.github.unchama.targetedeffect.unfocusedEffect
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * [ButtonEffect]に渡すScoped Lambdaの中で実行可能であるべきメソッドを提供するスコープオブジェクトのクラス.
 */
data class ButtonEffectScope(val event: InventoryClickEvent) {
  fun overwriteCurrentViewBy(newLayout: IndexedSlotLayout) = unfocusedEffect {
    (event.inventory.holder as MenuSession).overwriteViewWith(newLayout)
  }
}