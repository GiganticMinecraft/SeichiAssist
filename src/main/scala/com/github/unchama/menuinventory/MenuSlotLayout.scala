package com.github.unchama.menuinventory

import cats.effect.{ContextShift, IO}
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect.{TargetedEffect, emptyEffect}
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * どのインデックスがどの[Slot]と関連付けられているかの情報を持つ[Map]のラッパークラス.
 */
case class MenuSlotLayout(private[menuinventory] val layoutMap: Map[Int, Slot]) {
  /**
   * @return クリックされた枠に対応した[Slot]が[InventoryClickEvent]に基づいて引き起こす作用
   */
  def effectOn(event: InventoryClickEvent)(implicit cs: ContextShift[IO]): TargetedEffect[Player] =
    layoutMap.get(event.getSlot) match {
      case Some(slot) => slot.effectOn(event)
      case None => emptyEffect
    }

  /**
   * このレイアウトに[another]を「かぶせた」新しいレイアウトを作成する。
   *
   * 新しいレイアウトのスロットは, 同じ場所が[another]で埋まっている場合[another]のものが,
   * そうでなければこのレイアウトのものがセットされている.
   */
  def merge(another: MenuSlotLayout): MenuSlotLayout = MenuSlotLayout(layoutMap ++ another.layoutMap)

  /**
   * [slotReplacement]でレイアウトの一箇所を置き換えた新しいレイアウトを計算する.
   */
  def altered(slotReplacement: (Int, Slot)): MenuSlotLayout = copy(layoutMap = layoutMap + slotReplacement)
}

object MenuSlotLayout {
  val emptyLayout = MenuSlotLayout()

  def apply(): MenuSlotLayout = MenuSlotLayout(Map[Int, Slot]())

  @inline def singleSlotLayout(indexedSlot: => (Int, Slot)): MenuSlotLayout = MenuSlotLayout(indexedSlot)

  def apply(mappings: (Int, Slot)*): MenuSlotLayout = MenuSlotLayout(Map(mappings: _*))

  def combinedLayout(layouts: MenuSlotLayout*): MenuSlotLayout =
    layouts.toList.foldLeft(emptyLayout) { case (acc, layout) => acc.merge(layout) }
}
