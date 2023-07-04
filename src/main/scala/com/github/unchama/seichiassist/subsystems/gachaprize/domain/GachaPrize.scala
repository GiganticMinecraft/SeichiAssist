package com.github.unchama.seichiassist.subsystems.gachaprize.domain

import com.github.unchama.generic.Cloneable
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.GachaEvent

/**
 * @param itemStack ガチャで排出されるアイテム。
 * @param probability ガチャで排出される確率
 * @param signOwner 記名する場合はtrueにしてください
 * @param gachaEvent ガチャイベントで排出されるアイテムの場合は設定してください。
 *                       `None`の場合は通常排出アイテムとして扱います。
 */
case class GachaPrize[ItemStack: Cloneable](
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
