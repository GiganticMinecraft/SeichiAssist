package com.github.unchama.menuinventory

import cats.effect.{ContextShift, IO}
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import kotlin.collections.IndexedValue
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}

/**
 * どのインデックスがどの[Slot]と関連付けられているかの情報を持つ[Map]のラッパークラス.
 */
case class IndexedSlotLayout(private val map: Map[Int, Slot]) {
  /**
   * クリックされた枠に対応した[Slot]が[InventoryClickEvent]に基づいて引き起こす作用を計算する.
   */
  def computeAsyncEffectOn(event: InventoryClickEvent): TargetedEffect[Player] =
    map.get(event.getSlot) match {
      case Some(slot) => slot.effectOn(event)
      case None => EmptyEffect
    }

  /**
   * 指定した[Inventory]に[Slot]により構成されたレイアウトを敷き詰める.
   */
  def setItemsOn(inventory: Inventory): IO[Unit] = {
    import cats.implicits._

    import scala.concurrent.ExecutionContext

    val effects = for (slotIndex <- 0 until inventory.getSize)
      yield IO {
        val itemStack = map.get(slotIndex).map(_.itemStack).getOrElse(new ItemStack(Material.AIR))
        inventory.setItem(slotIndex, itemStack)
      }

    implicit val context: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    effects.toList.parSequence_
  }

  /**
   * このレイアウトに[another]を「かぶせた」新しいレイアウトを作成する。
   *
   * 新しいレイアウトのスロットは, 同じ場所が[another]で埋まっている場合[another]のものが,
   * そうでなければこのレイアウトのものがセットされている.
   */
  def merge(another: IndexedSlotLayout): IndexedSlotLayout = IndexedSlotLayout(map ++ another.map)

  /**
   * [slotReplacement]でレイアウトの一箇所を置き換えた新しいレイアウトを計算する.
   */
  def altered(slotReplacement: (Int, Slot)) = copy(map = map + slotReplacement)
}

object IndexedSlotLayout {
  val emptyLayout = IndexedSlotLayout()

  def apply() = IndexedSlotLayout(Map[Int, Slot]())

  def apply(mappings: (Int, Slot)*): IndexedSlotLayout = { IndexedSlotLayout(Map(mappings: _*)) }

  def apply(mapping: Iterable[IndexedValue[Slot]]) = IndexedSlotLayout(mapping.map(v => (v.component1(), v.component2())).toSeq: _*)

  @inline def singleSlotLayout(indexedSlot: => (Int, Slot)): IndexedSlotLayout = IndexedSlotLayout(indexedSlot)

  def combinedLayout(layouts: IndexedSlotLayout*): IndexedSlotLayout =
    layouts.toList.foldLeft(emptyLayout) { case (acc, layout) => acc.merge(layout) }
}
