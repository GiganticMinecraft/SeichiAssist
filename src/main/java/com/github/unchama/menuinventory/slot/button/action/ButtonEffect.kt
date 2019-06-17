package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
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
                        private val effect: (InventoryClickEvent) -> TargetedEffect<Player>) {
  /**
   * [event]に基づいて[effect]による作用を発生させる.
   * [effect]は[Player]に
   */
  fun runAsyncEffectOn(event: InventoryClickEvent): TargetedEffect<Player> =
      if (clickEventFilter.shouldReactTo(event)) effect(event) else EmptyEffect
}
