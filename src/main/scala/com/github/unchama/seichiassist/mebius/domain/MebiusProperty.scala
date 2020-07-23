package com.github.unchama.seichiassist.mebius.domain

import cats.effect.IO

import scala.util.Random

/**
 * @param ownerPlayerId         オーナーのプレーヤーID
 * @param enchantmentLevel      付与されるエンチャントとレベルのMap
 * @param level                 Mebiusのレベル
 * @param ownerNicknameOverride オーナーをMebiusがどう呼ぶか
 * @param mebiusName            Mebius自体の名前
 */
case class MebiusProperty(ownerPlayerId: String,
                          enchantmentLevel: Map[MebiusEnchantment, Int] = Map(),
                          level: MebiusLevel = MebiusLevel(1),
                          ownerNicknameOverride: Option[String] = None,
                          mebiusName: String = "MEBIUS") {
  require {
    enchantmentLevel.forall { case (MebiusEnchantment(_, unlockLevel, maxLevel, _), enchantmentLevel) =>
      unlockLevel.value >= level.value && 1 <= enchantmentLevel && enchantmentLevel <= maxLevel
    }
  }

  def incrementLevel: MebiusProperty = copy(level = level.increment)

  def randomlyUpgradeEnchantment(availableEnchantments: Set[MebiusEnchantment]): IO[MebiusProperty] = {
    val upgradableEnchantments = availableEnchantments.filter { mebiusEnchantment =>
      enchantmentLevel.get(mebiusEnchantment)
        .forall { currentLevel =>
          currentLevel < mebiusEnchantment.maxLevel
        }
    }.toSeq

    IO {
      val choice = upgradableEnchantments(Random.nextInt(upgradableEnchantments.size))
      val newLevel = enchantmentLevel.get(choice).map(_ + 1).getOrElse(1)

      this.copy(enchantmentLevel = enchantmentLevel.updated(choice, newLevel))
    }
  }

  def ownerNickname: String = ownerNicknameOverride.getOrElse(ownerPlayerId)

  /**
   * `another` と異なる [[MebiusEnchantment]] を返す。
   */
  def enchantmentDifferentFrom(another: MebiusProperty): Option[MebiusEnchantment] =
    another.enchantmentLevel.keySet.union(enchantmentLevel.keySet)
      .find { e => another.enchantmentLevel.get(e) != enchantmentLevel.get(e) }
}
