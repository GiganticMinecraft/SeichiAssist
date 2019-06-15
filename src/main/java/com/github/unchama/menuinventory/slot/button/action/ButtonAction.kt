package com.github.unchama.menuinventory.slot.button.action

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * メニューインベントリ上のボタンが及ぼす作用の情報を持つデータ型.
 *
 * @param clickEventFilter InventoryClickEventを受け取り動作を行わせるかを決定する [ClickEventFilter]
 * @param action  InventoryClickEventを受け取り何かしらの作用を発生させる関数
 *
 * [action]は[clickEventFilter] がtrueを返した際に呼び出されます.
 *
 * [action] が呼び出される時点で, 引数の[InventoryClickEvent]の[InventoryClickEvent.getWhoClicked] は
 * [Player] であることが保証されています.
 *
 * @author karayuu
 */
data class ButtonAction(private val clickEventFilter: ClickEventFilter,
                        private val action: (InventoryClickEvent) -> Unit) {
  operator fun invoke(event: InventoryClickEvent) {
    if (clickEventFilter.shouldReactTo(event)) action(event)
  }
}
