package com.github.unchama.menuinventory

import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.collection.toMap
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * どのインデックスがどの[Slot]と関連付けられているかの情報を持つ[Map]のラッパークラス.
 */
data class IndexedSlotLayout(private val map: Map<Int, Slot>) {
  constructor(vararg mappings: Pair<Int, Slot>): this(mapOf(*mappings))

  constructor(mapping: Iterable<IndexedValue<Slot>>): this(mapping.toMap())

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
        launch {
          val itemStack = map[slotIndex]?.itemStack ?: ItemStack(Material.AIR)
          inventory.setItem(slotIndex, itemStack)
        }
      }
    }
  }

  /**
   * このレイアウトに[another]を「かぶせた」新しいレイアウトを作成する。
   *
   * 新しいレイアウトのスロットは, 同じ場所が[another]で埋まっている場合[another]のものが,
   * そうでなければこのレイアウトのものがセットされている.
   */
  fun merge(another: IndexedSlotLayout): IndexedSlotLayout = IndexedSlotLayout(map.plus(another.map))

  /**
   * [slotReplacement]でレイアウトの一箇所を置き換えた新しいレイアウトを計算する.
   */
  internal fun altered(slotReplacement: Pair<Int, Slot>) = copy(map = map + slotReplacement)
}

val emptyLayout = IndexedSlotLayout()

inline fun singleSlotLayout(indexedSlot: () -> Pair<Int, Slot>): IndexedSlotLayout = IndexedSlotLayout(indexedSlot())

fun combinedLayout(vararg layouts: IndexedSlotLayout): IndexedSlotLayout =
    layouts.toList().fold(emptyLayout) { acc, layout -> acc.merge(layout) }
