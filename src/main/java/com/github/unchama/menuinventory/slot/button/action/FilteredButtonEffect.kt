package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * "フィルタ"付きの[ButtonEffect]
 *
 * @param clickEventFilter InventoryClickEventを受け取り動作を行わせるかを決定する [ClickEventFilter]
 * @param effect InventoryClickEventを受け取り何かしらの作用を発生させる関数
 *
 * [effect]は[clickEventFilter] がtrueを返した際に発火されます.
 */
data class FilteredButtonEffect(private val clickEventFilter: ClickEventFilter,
                                private val effect: ButtonEffectScope.() -> TargetedEffect<Player>): ButtonEffect {
  /**
   * [event]に基づいた[effect]による作用を計算する.
   */
  override fun asyncEffectOn(event: InventoryClickEvent): TargetedEffect<Player> {
    return if (clickEventFilter.shouldReactTo(event)) {
      effect(ButtonEffectScope(event))
    } else {
      EmptyEffect
    }
  }

}