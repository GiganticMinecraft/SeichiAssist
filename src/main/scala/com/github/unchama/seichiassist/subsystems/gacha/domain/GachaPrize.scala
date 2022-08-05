package com.github.unchama.seichiassist.subsystems.gacha.domain

/**
 * @param itemStack ガチャで排出されるアイテム。
 * @param probability ガチャで排出される確率
 * @param hasOwner 記名する場合はtrueにしてください
 */
case class GachaPrize[ItemStack](
  itemStack: ItemStack,
  probability: GachaProbability,
  hasOwner: Boolean,
  id: GachaPrizeId
)
