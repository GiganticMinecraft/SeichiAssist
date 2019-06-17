package com.github.unchama.menuinventory

import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

data class IndexedSlotLayout(val map: Map<Int, Slot>) {
  constructor(vararg mappings: Pair<Int, Slot>): this(mapOf(*mappings))

  internal fun computeAsyncEffectOn(event: InventoryClickEvent): TargetedEffect<Player> {
    return map[event.slot]?.computeEffectOn(event) ?: EmptyEffect
  }

  internal suspend fun asynchronouslySetItemsOn(inventory: Inventory) {
    coroutineScope {
      for (slotIndex in 0 until inventory.size) {
        async {
          val itemStack = map[slotIndex]?.itemStack ?: ItemStack(Material.AIR)
          inventory.setItem(slotIndex, itemStack)
        }
      }
    }
  }
}
