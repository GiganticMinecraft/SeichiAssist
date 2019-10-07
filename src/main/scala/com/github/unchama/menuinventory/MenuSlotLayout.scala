package com.github.unchama.menuinventory

import cats.effect.{ContextShift, IO}
import com.github.unchama.menuinventory.Types.LayoutPreparationContext
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}

/**
 * どのインデックスがどの[Slot]と関連付けられているかの情報を持つ[Map]のラッパークラス.
 */
case class MenuSlotLayout(private val map: Map[Int, Slot]) {
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
  def setItemsOn(inventory: Inventory)(implicit ctx: LayoutPreparationContext): IO[Unit] = {
    import cats.implicits._

    val effects = for (slotIndex <- 0 until inventory.getSize)
      yield IO {
        val itemStack = map.get(slotIndex).map(_.itemStack).getOrElse(new ItemStack(Material.AIR))
        inventory.setItem(slotIndex, itemStack)
      }

    implicit val context: ContextShift[IO] = IO.contextShift(ctx)
    effects.toList.parSequence_
  }

  /**
   * このレイアウトに[another]を「かぶせた」新しいレイアウトを作成する。
   *
   * 新しいレイアウトのスロットは, 同じ場所が[another]で埋まっている場合[another]のものが,
   * そうでなければこのレイアウトのものがセットされている.
   */
  def merge(another: MenuSlotLayout): MenuSlotLayout = MenuSlotLayout(map ++ another.map)

  /**
   * [slotReplacement]でレイアウトの一箇所を置き換えた新しいレイアウトを計算する.
   */
  def altered(slotReplacement: (Int, Slot)): MenuSlotLayout = copy(map = map + slotReplacement)
}

object MenuSlotLayout {
  val emptyLayout = MenuSlotLayout()

  def apply(): MenuSlotLayout = MenuSlotLayout(Map[Int, Slot]())

  @inline def singleSlotLayout(indexedSlot: => (Int, Slot)): MenuSlotLayout = MenuSlotLayout(indexedSlot)

  def apply(mappings: (Int, Slot)*): MenuSlotLayout = MenuSlotLayout(Map(mappings: _*))

  def combinedLayout(layouts: MenuSlotLayout*): MenuSlotLayout =
    layouts.toList.foldLeft(emptyLayout) { case (acc, layout) => acc.merge(layout) }
}
