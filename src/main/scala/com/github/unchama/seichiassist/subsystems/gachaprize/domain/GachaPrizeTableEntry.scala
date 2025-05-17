package com.github.unchama.seichiassist.subsystems.gachaprize.domain

import com.github.unchama.generic.Cloneable
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.GachaEvent

/**
 * @param itemStack ガチャで排出されるアイテム。
 * @param probability ガチャで排出される確率
 * @param signOwner ガチャ景品に対して記名をする場合はtrue、しない場合はfalse
 * @param gachaEvent このガチャ景品をイベント排出景品として扱う場合に紐付けるイベント情報
 *                   Noneの場合はイベントが開催されていない場合に排出されるアイテムとして扱われます。
 */
case class GachaPrizeTableEntry[ItemStack: Cloneable](
  itemStack: ItemStack,
  probability: GachaProbability,
  signOwner: Boolean,
  id: GachaPrizeId,
  gachaEvent: Option[GachaEvent]
) {

  def materializeWithOwnerSignature(
    ownerName: String
  )(implicit sign: CanBeSignedAsGachaPrize[ItemStack]): ItemStack = {
    if (signOwner) sign.signWith(ownerName)(this)
    else Cloneable[ItemStack].clone(itemStack)
  }

  def isGachaEventItem: Boolean = gachaEvent.nonEmpty

  def nonGachaEventItem: Boolean = gachaEvent.isEmpty

}
