package com.github.unchama.seichiassist.mebius.domain

import cats.effect.IO

import scala.util.Random

/**
 * @param ownerName     オーナーのプレーヤーID
 * @param enchantments  付与されるエンチャントメント
 * @param level         Mebiusのレベル
 * @param ownerNickname オーナーをMebiusがどう呼ぶか
 * @param mebiusName    Mebius自体の名前
 */
case class MebiusProperty(ownerName: String,
                          // FIXME rename to enchantmentLevels
                          enchantments: Map[MebiusEnchantment, Int],
                          level: MebiusLevel,
                          ownerNickname: Option[String],
                          mebiusName: String = "MEBIUS") {
  require {
    enchantments.forall { case (MebiusEnchantment(_, unlockLevel, maxLevel, _), enchantmentLevel) =>
      unlockLevel.value >= level.value && 1 <= enchantmentLevel && enchantmentLevel <= maxLevel
    }
  }

  def incrementLevel: MebiusProperty = copy(level = level.increment)

  def randomlyAugmentEnchantment(availableEnchantments: Set[MebiusEnchantment]): IO[MebiusProperty] = {
    val upgradableEnchantments = availableEnchantments.filter { mebiusEnchantment =>
      enchantments
        .get(mebiusEnchantment)
        .forall { currentLevel =>
          currentLevel < mebiusEnchantment.maxLevel
        }
    }.toSeq

    IO {
      val choice = upgradableEnchantments(Random.nextInt(upgradableEnchantments.size))
      val newLevel = enchantments.get(choice).map(_ + 1).getOrElse(1)

      this.copy(enchantments = enchantments.updated(choice, newLevel))
    }
  }

  /**
   * `another` と異なる [[MebiusEnchantment]] を返す。
   */
  def enchantmentDifferentFrom(another: MebiusProperty): Option[MebiusEnchantment] =
    another.enchantments.keySet.union(enchantments.keySet)
      .find { e => another.enchantments.get(e) != enchantments.get(e) }
}
