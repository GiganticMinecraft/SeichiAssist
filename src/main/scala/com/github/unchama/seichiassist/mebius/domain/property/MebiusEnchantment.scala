package com.github.unchama.seichiassist.mebius.domain.property

import enumeratum._
import org.bukkit.enchantments.Enchantment

/**
 * Mebiusに付与できるエンチャントのクラス
 */
sealed abstract class MebiusEnchantment(val enchantment: Enchantment,
                                        val unlockLevel: MebiusLevel,
                                        val maxLevel: Int,
                                        val displayName: String) extends EnumEntry {
  require {
    unlockLevel.value <= MebiusLevel.max && maxLevel >= 1
  }
}

object MebiusEnchantment extends Enum[MebiusEnchantment] {

  case object Protection
    extends MebiusEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, MebiusLevel(2), 10, "ダメージ軽減")

  case object Durability
    extends MebiusEnchantment(Enchantment.DURABILITY, MebiusLevel(1), 10, "耐久力")

  case object Mending
    extends MebiusEnchantment(Enchantment.MENDING, MebiusLevel(1), 1, "修繕")

  case object FireProtection
    extends MebiusEnchantment(Enchantment.PROTECTION_FIRE, MebiusLevel(6), 10, "火炎耐性")

  case object ProjectileProtection
    extends MebiusEnchantment(Enchantment.PROTECTION_PROJECTILE, MebiusLevel(6), 10, "飛び道具耐性")

  case object ExplosionProtection
    extends MebiusEnchantment(Enchantment.PROTECTION_EXPLOSIONS, MebiusLevel(6), 10, "爆発耐性")

  case object Respiration
    extends MebiusEnchantment(Enchantment.OXYGEN, MebiusLevel(15), 3, "水中呼吸")

  case object WaterAffinity
    extends MebiusEnchantment(Enchantment.WATER_WORKER, MebiusLevel(15), 1, "水中採掘")

  def unapply(mebiusEnchantment: MebiusEnchantment): Some[(Enchantment, MebiusLevel, Int, String)] = {
    import mebiusEnchantment._
    Some((enchantment, unlockLevel, maxLevel, displayName))
  }

  override val values: IndexedSeq[MebiusEnchantment] = findValues

}
