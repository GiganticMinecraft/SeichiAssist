package com.github.unchama.seichiassist.subsystems.gacha.domain

trait GachaPrizeEncoder[ItemStack] {

  def encode(
    string: String,
    gachaProbability: GachaProbability,
    hasOwner: Boolean,
    gachaPrizeId: GachaPrizeId
  ): GachaPrize[ItemStack]

  def decode(gachaPrize: GachaPrize[ItemStack]): String

}
