package com.github.unchama.seichiassist.subsystems.gacha.domain

import org.bukkit.inventory.ItemStack

/**
 * @param itemStack ガチャで排出されるアイテム。数もそのまま利用されます
 * @param probability ガチャで排出される確率
 * @param isAppendOwner 記名する場合はtrueにしてください
 */
case class GachaPrize(
  itemStack: ItemStack,
  probability: Double,
  isAppendOwner: Boolean,
  id: GachaPrizeId
)
