package com.github.unchama.seichiassist.mebius.domain.property

import org.bukkit.enchantments.Enchantment

/**
 * Mebiusに付与できるエンチャントのクラス
 */
case class MebiusEnchantment(enchantment: Enchantment, unlockLevel: MebiusLevel, maxLevel: Int, displayName: String) {
  require {
    unlockLevel.value <= MebiusLevel.max && maxLevel >= 1
  }
}
