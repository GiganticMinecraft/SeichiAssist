package com.github.unchama.menuinventory

/**
 * どのインデックスがどの[Slot]と関連付けられているかの情報を持つ[Map]のラッパークラス.
 */
case class IndexedSlotLayout(private val map: Map[Int, Slot]) {
  def this(): this(mapOf())

  def this(vararg mappings: Pair[Int, Slot]): this(mapOf(*mappings))

  def this(mapping: Iterable[IndexedValue[Slot]]): this(mapping.toMap())

  /**
   * クリックされた枠に対応した[Slot]が[InventoryClickEvent]に基づいて引き起こす作用を計算する.
   */
  internal def computeAsyncEffectOn(event: InventoryClickEvent): TargetedEffect[Player] = {
    return map[event.slot]?.effectOn(event) ?: EmptyEffect
  }

  /**
   * 指定した[Inventory]に[Slot]により構成されたレイアウトを敷き詰める.
   */
  internal suspend def asynchronouslySetItemsOn(inventory: Inventory) {
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
  def merge(another: IndexedSlotLayout): IndexedSlotLayout = IndexedSlotLayout(map.plus(another.map))

  /**
   * [slotReplacement]でレイアウトの一箇所を置き換えた新しいレイアウトを計算する.
   */
  internal def altered(slotReplacement: Pair[Int, Slot]) = copy(map = map + slotReplacement)
}

object IndexedSlotLayout {
  val emptyLayout = IndexedSlotLayout()

  inline def singleSlotLayout(indexedSlot: () => Pair[Int, Slot]): IndexedSlotLayout = IndexedSlotLayout(indexedSlot())

  def combinedLayout(vararg layouts: IndexedSlotLayout): IndexedSlotLayout =
    layouts.toList().fold(emptyLayout) { acc, layout => acc.merge(layout) }
}
