package com.github.unchama.seichiassist.mebius.domain.property

import enumeratum._

/**
 * Mebiusに付与できるエンチャントのクラス
 */
sealed abstract class MebiusEnchantment(val unlockLevel: MebiusLevel,
                                        val maxLevel: Int,
                                        val displayName: String) extends EnumEntry {
  require {
    unlockLevel.value <= MebiusLevel.max && maxLevel >= 1
  }
}

object MebiusEnchantment extends Enum[MebiusEnchantment] {

  case object Protection extends MebiusEnchantment(MebiusLevel(2), 10, "ダメージ軽減")

  case object Durability extends MebiusEnchantment(MebiusLevel(1), 10, "耐久力")

  case object Mending extends MebiusEnchantment(MebiusLevel(1), 1, "修繕")

  case object FireProtection extends MebiusEnchantment(MebiusLevel(6), 10, "火炎耐性")

  case object ProjectileProtection extends MebiusEnchantment(MebiusLevel(6), 10, "飛び道具耐性")

  case object ExplosionProtection extends MebiusEnchantment(MebiusLevel(6), 10, "爆発耐性")

  case object Respiration extends MebiusEnchantment(MebiusLevel(15), 3, "水中呼吸")

  case object WaterAffinity extends MebiusEnchantment(MebiusLevel(15), 1, "水中採掘")

  def unapply(mebiusEnchantment: MebiusEnchantment): Some[(MebiusLevel, Int, String)] = {
    import mebiusEnchantment._
    Some((unlockLevel, maxLevel, displayName))
  }

  override val values: IndexedSeq[MebiusEnchantment] = findValues

}
