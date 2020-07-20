package com.github.unchama.seichiassist.mebius.domain

case class MebiusProperty(ownerName: String, enchantments: Map[MebiusEnchantment, Int],
                          level: MebiusLevel, name: Option[String]) {
  require {
    enchantments.forall { case (MebiusEnchantment(_, unlockLevel, maxLevel, _), enchantmentLevel) =>
      unlockLevel.value >= level.value && 1 <= enchantmentLevel && enchantmentLevel <= maxLevel
    }
  }
}
