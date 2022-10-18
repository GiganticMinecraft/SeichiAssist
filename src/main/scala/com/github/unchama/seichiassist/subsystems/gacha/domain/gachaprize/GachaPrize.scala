package com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize

import com.github.unchama.seichiassist.subsystems.gacha.domain.{CanBeSignedAsGachaPrize, GachaProbability}

/**
 * @param itemStack ガチャで排出されるアイテム。
 * @param probability ガチャで排出される確率
 * @param signOwner 記名する場合はtrueにしてください
 */
case class GachaPrize[ItemStack](
  itemStack: ItemStack,
  probability: GachaProbability,
  signOwner: Boolean,
  id: GachaPrizeId
) {

  def materializeWithOwnerSignature(
    ownerName: String
  )(implicit sign: CanBeSignedAsGachaPrize[ItemStack]): ItemStack = {
    if (signOwner) sign.signWith(ownerName)(this)
    else this.itemStack
  }

}
