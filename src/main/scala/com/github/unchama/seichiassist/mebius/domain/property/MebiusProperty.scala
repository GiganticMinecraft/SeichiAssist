package com.github.unchama.seichiassist.mebius.domain.property

import cats.effect.IO

import scala.util.Random

/**
 * @param ownerPlayerId         オーナーのプレーヤーID
 * @param ownerUuid             オーナーのUUID文字列
 * @param enchantmentLevel      付与されるエンチャントとレベルのMap
 * @param level                 Mebiusのレベル
 * @param ownerNicknameOverride オーナーをMebiusがどう呼ぶか
 * @param mebiusName            Mebius自体の名前
 */
case class MebiusProperty(ownerPlayerId: String,
                          ownerUuid: String,
                          enchantmentLevel: Map[MebiusEnchantment, Int] = Map(),
                          level: MebiusLevel = MebiusLevel(1),
                          ownerNicknameOverride: Option[String] = None,
                          mebiusName: String = "MEBIUS") {

  import MebiusLevel.mebiusLevelOrder._

  enchantmentLevel.foreach { case m@(MebiusEnchantment(_, unlockLevel, maxLevel, _), enchantmentLevel) =>
    require({
      unlockLevel <= level
    }, s"unlockLevel = $unlockLevel <= $level = level for $m")

    require({
      1 <= enchantmentLevel && enchantmentLevel <= maxLevel
    }, s"$enchantmentLevel is in [1, $maxLevel] for $m")
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
