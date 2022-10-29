package com.github.unchama.seichiassist.subsystems.gacha.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaevent.GachaEventName

/**
 * @param itemStack ガチャで排出されるアイテム。
 * @param probability ガチャで排出される確率
 * @param signOwner 記名する場合はtrueにしてください
 * @param gachaEventName ガチャイベントで排出されるアイテムの場合は設定してください。
 *                       `None`の場合は通常排出アイテムとして扱います。
 */
case class GachaPrize[ItemStack](
  itemStack: ItemStack,
  probability: GachaProbability,
  signOwner: Boolean,
  id: GachaPrizeId,
  gachaEventName: Option[GachaEventName]
) {

  def materializeWithOwnerSignature(
    ownerName: String
  )(implicit sign: CanBeSignedAsGachaPrize[ItemStack]): ItemStack = {
    if (signOwner) sign.signWith(ownerName)(this)
    else this.itemStack
  }

}
