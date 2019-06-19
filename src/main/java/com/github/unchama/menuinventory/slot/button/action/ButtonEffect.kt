package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * メニューインベントリ上のボタンが[InventoryClickEvent]に対して発火すべき作用を計算するオブジェクトへのinterface.
 */
interface ButtonEffect {

  /**
   * [event]に基づいてボタンが発生させるべき作用を計算する.
   */
  fun asyncEffectOn(event: InventoryClickEvent): TargetedEffect<Player>

  companion object {
    operator fun invoke(effect: ButtonEffectScope.() -> TargetedEffect<Player>): ButtonEffect = object : ButtonEffect {
      override fun asyncEffectOn(event: InventoryClickEvent): TargetedEffect<Player> = effect(ButtonEffectScope(event))
    }
  }

}

