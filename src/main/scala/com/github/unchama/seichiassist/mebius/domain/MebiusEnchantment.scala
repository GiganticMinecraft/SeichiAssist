package com.github.unchama.seichiassist.mebius.domain

import org.bukkit.enchantments.Enchantment

/**
 * Mebiusに付与できるエンチャントのクラス
 */
case class MebiusEnchantment(enchantment: Enchantment, unlockLevel: Int, maxLevel: Int, displayName: String)
