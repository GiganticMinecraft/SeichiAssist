package com.github.unchama.menuinventory.slot.button.action

import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.{EmptyEffect, TargetedEffect}
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
case class FilteredButtonEffect(private val clickEventFilter: ClickEventFilter,
                                private val effect: (ButtonEffectScope) => TargetedEffect[Player]) extends ButtonEffect {

  /**
   * [ButtonEffectScope]に依存しない[TargetedEffect]を実行する[FilteredButtonEffect]を構築する.
   */
  def this(clickEventFilter: ClickEventFilter, effects: targetedeffect.TargetedEffect[Player]*) {
    this(clickEventFilter, { sequentialEffect(effects: _*) })
  }

  /**
   * [event]に基づいた[effect]による作用を計算する.
   */
  override def asyncEffectOn(event: InventoryClickEvent): TargetedEffect[Player] = {
    return if (clickEventFilter.shouldReactTo(event)) {
      effect(ButtonEffectScope(event))
    } else {
      EmptyEffect
    }
  }

}

/**
 * 左クリックに限定した[FilteredButtonEffect]
 */
case class LeftClickButtonEffect(private val effect: (ButtonEffectScope) => TargetedEffect[Player]) extends FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, effect) {

  /**
   * [ButtonEffectScope]に依存しない[TargetedEffect]を実行する[LeftClickButtonEffect]を構築する.
   */
  def this(effects: TargetedEffect[Player]*) {
    this({ sequentialEffect(effects: _*) })
  }
}
