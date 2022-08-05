package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import com.github.unchama.seichiassist.subsystems.gacha.bukkit.codec.ItemStackCodec
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrize,
  GachaPrizeEncoder,
  GachaPrizeId,
  GachaProbability
}
import org.bukkit.inventory.ItemStack

object BukkitBuildGachaPrizeEncoder extends GachaPrizeEncoder[ItemStack] {

  override def encode(
    itemStackString: String,
    gachaProbability: GachaProbability,
    hasOwner: Boolean,
    gachaPrizeId: GachaPrizeId
  ): GachaPrize[ItemStack] = GachaPrize(
    ItemStackCodec.fromString(itemStackString),
    gachaProbability,
    hasOwner,
    gachaPrizeId
  )

  override def decode(gachaPrize: GachaPrize[ItemStack]): String =
    ItemStackCodec.toString(gachaPrize.itemStack)
}
