package com.github.unchama.menuinventory

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

/**
 * 共有された[sessionInventory]を作用付きの「メニュー」として扱うインベントリを保持するためのセッション.
 */
class MenuSession internal constructor(view: MenuInventoryView): InventoryHolder {
  private val sessionInventory = view.createConfiguredInventory(this)

  var view: MenuInventoryView = view
    private set

  internal suspend fun overwriteViewWith(layout: IndexedSlotLayout) {
    view = view.copy(slotLayout = layout)
    view.slotLayout.asynchronouslySetItemsOn(sessionInventory)
  }

  override fun getInventory() = sessionInventory

  /**
   * このセッションが持つ共有インベントリを開く作用を返します.
   */
  val openSessionInventoryEffect: TargetedEffect<Player> = TargetedEffect {
    it.openInventory(sessionInventory)
  }

}