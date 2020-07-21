package com.github.unchama.seichiassist.mebius.domain

/**
 * @param ownerName     オーナーのプレーヤーID
 * @param enchantments  付与されるエンチャントメント
 * @param level         Mebiusのレベル
 * @param ownerNickname オーナーをMebiusがどう呼ぶか
 */
case class MebiusProperty(ownerName: String, enchantments: Map[MebiusEnchantment, Int],
                          level: MebiusLevel, ownerNickname: Option[String]) {
  require {
    enchantments.forall { case (MebiusEnchantment(_, unlockLevel, maxLevel, _), enchantmentLevel) =>
      unlockLevel.value >= level.value && 1 <= enchantmentLevel && enchantmentLevel <= maxLevel
    }
  }
}
