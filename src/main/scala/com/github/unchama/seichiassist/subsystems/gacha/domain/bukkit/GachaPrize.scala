package com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizeId
import com.github.unchama.util.bukkit.ItemStackUtil.appendOwnerInformation
import org.bukkit.entity.Player
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
) {

  def getGiveItemStack[F[_]: Sync](name: Option[String]): F[ItemStack] = {
    Sync[F].delay {
      val clonedItemStack = itemStack.clone()
      val givenItem =
        if (isAppendOwner && name.nonEmpty)
          appendOwnerInformation(name.get)(clonedItemStack)
        else clonedItemStack
      givenItem
    }
  }

}
