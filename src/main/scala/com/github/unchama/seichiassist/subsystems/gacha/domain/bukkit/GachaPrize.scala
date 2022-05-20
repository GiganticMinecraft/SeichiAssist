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

  def createNewItem(owner: Option[String]): ItemStack = {
    val clonedItemStack = itemStack.clone()
    val givenItem =
      if (hasOwner && owner.nonEmpty)
        appendOwnerInformation(owner.get)(clonedItemStack)
      else clonedItemStack
    givenItem
  }

}
