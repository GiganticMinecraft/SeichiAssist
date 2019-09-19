package com.github.unchama.menuinventory.slot.button.action

import org.bukkit.event.inventory.InventoryClickEvent

/**
 * @author karayuu
 */
enum class ClickEventFilter(private val predicate: (InventoryClickEvent) -> Boolean) {
  /**
   * 左クリックを表す [ClickEventFilter] です.
   */
  LEFT_CLICK({ it.isLeftClick }),

  /**
   * 右クリックを表す [ClickEventFilter] です.
   */
  RIGHT_CLICK({ it.isRightClick }),

  /**
   * 常に `true` を返す [ClickEventFilter] です
   */
  ALWAYS_INVOKE({ true });

  /**
   * 与えられた [InventoryClickEvent] に対して動作を行うべきか返します.
   *
   * @param event [InventoryClickEvent]
   * @return true: 動作を行う / false: 動作を行わない
   */
  def shouldReactTo(event: InventoryClickEvent): Boolean = predicate(event)
}
