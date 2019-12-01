package com.github.unchama.menuinventory.slot

import cats.effect.{ContextShift, IO}
import com.github.unchama.targetedeffect.{TargetedEffect, emptyEffect}
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
trait Slot {
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
  def effectOn(event: InventoryClickEvent)(implicit cs: ContextShift[IO]): TargetedEffect[Player]
}

object Slot {
  /**
   * クリックしたときにイベントをキャンセルすることもせず
   * 何も追加の作用を発生させない, [itemStack]が入っただけの[Slot]を作成する.
   */
  def plainSlotWith(itemStack: ItemStack): Slot = new Slot {
    override val itemStack: ItemStack = itemStack

    override def effectOn(event: InventoryClickEvent)(implicit cs: ContextShift[IO]): emptyEffect.type =
      emptyEffect
  }
}