package com.github.unchama.menuinventory.slot.button.action

import org.bukkit.event.inventory.InventoryClickEvent

/**
 * @author karayuu
 */
case class ClickEventFilter(private val predicate: InventoryClickEvent => Boolean) extends AnyVal {
  /**
   * 与えられた [InventoryClickEvent] に対して動作を行うべきか返します.
   *
   * @param event [InventoryClickEvent]
   * @return true: 動作を行う / false: 動作を行わない
   */
  def shouldReactTo(event: InventoryClickEvent): Boolean = predicate(event)
}

object ClickEventFilter {
  val LEFT_CLICK = ClickEventFilter(_.isLeftClick)
  val RIGHT_CLICK = ClickEventFilter(_.isRightClick)
  val ALWAYS_INVOKE = ClickEventFilter(_ => true)
}
