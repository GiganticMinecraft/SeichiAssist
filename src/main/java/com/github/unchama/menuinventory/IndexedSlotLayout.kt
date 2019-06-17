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

/**
 * どのインデックスがどの[Slot]と関連付けられているかの情報を持つ[Map]のラッパークラス.
 */
data class IndexedSlotLayout(val map: Map<Int, Slot>) {
  constructor(vararg mappings: Pair<Int, Slot>): this(mapOf(*mappings))

  /**
   * クリックされた枠に対応した[Slot]が[InventoryClickEvent]に基づいて引き起こす作用を計算する.
   */
  internal fun computeAsyncEffectOn(event: InventoryClickEvent): TargetedEffect<Player> {
    return map[event.slot]?.effectOn(event) ?: EmptyEffect
  }

  /**
   * 指定した[Inventory]に[Slot]により構成されたレイアウトを敷き詰める.
   */
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
