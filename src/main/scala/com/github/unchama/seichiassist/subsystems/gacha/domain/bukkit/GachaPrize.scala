package com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit

import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrizeId, GachaProbability}
import com.github.unchama.util.bukkit.ItemStackUtil.appendOwnerInformation
import org.bukkit.inventory.ItemStack

/**
 * @param itemStack ガチャで排出されるアイテム。数もそのまま利用されます
 * @param probability ガチャで排出される確率
 * @param hasOwner 記名する場合はtrueにしてください
 */
case class GachaPrize(
  itemStack: ItemStack,
  probability: GachaProbability,
  hasOwner: Boolean,
  id: GachaPrizeId
) {

  def getGiveItemStack(name: Option[String]): ItemStack = {
    val clonedItemStack = itemStack.clone()
    val givenItem =
      if (hasOwner && name.nonEmpty)
        appendOwnerInformation(name.get)(clonedItemStack)
      else clonedItemStack
    givenItem
  }

}
