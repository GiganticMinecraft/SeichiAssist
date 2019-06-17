package com.github.unchama.menuinventory

import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

class MenuSession(view: MenuInventoryView): InventoryHolder {
  private val sessionInventory = view.getConfiguredInventory(this)

  var view: MenuInventoryView = view
    private set

  internal suspend fun overwriteViewWith(layout: IndexedSlotLayout) {
    view = view.copy(slotLayout = layout)
    view.slotLayout.asynchronouslySetItemsOn(sessionInventory)
  }

  override fun getInventory() = sessionInventory

  val openSessionInventoryEffect: TargetedEffect<Player> = TargetedEffect {
    it.openInventory(sessionInventory)
  }

}