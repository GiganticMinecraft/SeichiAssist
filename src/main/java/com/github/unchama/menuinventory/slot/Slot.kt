package com.github.unchama.menuinventory.slot

import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * [Slot]はインベントリUI上の一つの枠についての情報を持つオブジェクトです.
 *
 * [effectOn]の作用はクリックにより発生した[InventoryClickEvent]をキャンセル状態にすることができます.
 *
 * @author karayuu
 */
interface Slot {
  /**
   * この [Slot] にセットされている [ItemStack].
   */
  val itemStack: ItemStack

  /**
   * この [Slot] に定義された非同期で実行されて良い作用を [InventoryClickEvent] から計算します.
   *
   * このメソッド自体の呼び出しに副作用はありません.
   *
   * @param event [InventoryClickEvent]
   * @return クリックした[Player]へ及ぼすべき作用
   */
  fun effectOn(event: InventoryClickEvent): TargetedEffect<Player>

  companion object {
    /**
     * クリックしたときに何も反応しない, [itemStack]が入っただけの[Slot]を作成します.
     */
    fun eventless(itemStack: ItemStack): Slot = object : Slot {
      override val itemStack = itemStack
      override fun effectOn(event: InventoryClickEvent) = EmptyEffect
    }
  }
}
