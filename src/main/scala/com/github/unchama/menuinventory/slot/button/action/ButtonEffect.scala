package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * メニューインベントリ上のボタンが[InventoryClickEvent]に対して発火すべき作用を計算するオブジェクトへのtrait.
 */
trait ButtonEffect {

  /**
   * [event]に基づいてボタンが発生させるべき作用を計算する.
   */
  def asyncEffectOn(event: InventoryClickEvent): TargetedEffect[Player]

}

object ButtonEffect {
  def apply(effect: ButtonEffectScope => TargetedEffect[Player]): ButtonEffect =
    (event: InventoryClickEvent) => effect(ButtonEffectScope(event))
}

