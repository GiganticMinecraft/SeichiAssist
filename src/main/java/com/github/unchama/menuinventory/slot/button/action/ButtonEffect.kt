package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuSession
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.UnfocusedEffect
import com.github.unchama.targetedeffect.unfocusedEffect
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * メニューインベントリ上のボタンが及ぼす作用の情報を持つデータ型.
 *
 * @param clickEventFilter InventoryClickEventを受け取り動作を行わせるかを決定する [ClickEventFilter]
 * @param effect InventoryClickEventを受け取り何かしらの作用を発生させる関数
 *
 * [effect]は[clickEventFilter] がtrueを返した際に発火されます.
 *
 * @author karayuu
 */
data class ButtonEffect(private val clickEventFilter: ClickEventFilter,
                        private val effect: ButtonEffectContext.() -> TargetedEffect<Player>) {
  /**
   * [event]に基づいて[effect]による作用を発生させる.
   * [effect]は[Player]に
   */
  fun runAsyncEffectOn(event: InventoryClickEvent): TargetedEffect<Player> =
      if (clickEventFilter.shouldReactTo(event)) effect(ButtonEffectContext(event)) else EmptyEffect
}

data class ButtonEffectContext(val event: InventoryClickEvent) {
  fun overwriteCurrentViewBy(newLayout: IndexedSlotLayout) = unfocusedEffect {
    (event.inventory.holder as MenuSession).overwriteViewWith(newLayout)
  }
}
