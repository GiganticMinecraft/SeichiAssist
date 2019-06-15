package com.github.unchama.menuinventory.slot

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * [Slot]はインベントリUI上の一つの枠についての情報を持つオブジェクトです.
 *
 * [itemStack]は枠が表示するべきアイテムスタックを表します.

 * [runEffect]は, この枠がクリックされた際に及ぼすべき作用を発生させます.
 * 作用はクリックにより発生した[InventoryClickEvent]をキャンセル状態にすることができます.
 *
 * @author karayuu
 */
interface Slot {
  /**
   * この [Slot] にセットされている [ItemStack].
   */
  val itemStack: ItemStack

  /**
   * この [Slot] に定義された作用を [InventoryClickEvent] に基づいて発生させます.
   *
   * @param event [InventoryClickEvent]
   */
  fun runEffect(event: InventoryClickEvent)

  companion object {
    /**
     * クリックしたときに何も反応しない, [itemStack]が入っただけの[Slot]を作成します.
     */
    fun from(itemStack: ItemStack): Slot = object : Slot {
      override val itemStack = itemStack
      override fun runEffect(event: InventoryClickEvent) {}
    }
  }
}
