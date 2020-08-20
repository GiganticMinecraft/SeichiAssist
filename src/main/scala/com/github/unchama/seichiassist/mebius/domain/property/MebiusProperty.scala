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

  enchantmentLevel.foreach { case m@(MebiusEnchantment(unlockLevel, maxLevel, _), enchantmentLevel) =>
    require({
      unlockLevel <= level
    }, s"unlockLevel = $unlockLevel <= $level = level for $m")

    require({
      1 <= enchantmentLevel && enchantmentLevel <= maxLevel
    }, s"$enchantmentLevel is in [1, $maxLevel] for $m")
  }

  lazy val upgradeByOneLevel: IO[MebiusProperty] = {
    level.increment match {
      case Some(newLevel) =>
        val levelUpdatedProperty = copy(level = newLevel)

        if (newLevel.isMaximum) {
          IO.pure {
            levelUpdatedProperty.copy(
              enchantmentLevel = enchantmentLevel.updated(MebiusEnchantment.Unbreakable, 1)
            )
          }
        } else {
          val upgradableEnchantments = MebiusEnchantment
            .values
            .filter { mebiusEnchantment =>
              val upgradable = enchantmentLevel
                .get(mebiusEnchantment)
                .forall { currentLevel =>
                  currentLevel < mebiusEnchantment.maxLevel
                }

              val possiblyGrantedNewly = mebiusEnchantment.unlockLevel <= newLevel

              upgradable || possiblyGrantedNewly
            }

          IO {
            val choice = upgradableEnchantments(Random.nextInt(upgradableEnchantments.size))
            val newLevel = enchantmentLevel.get(choice).map(_ + 1).getOrElse(1)

            this.copy(enchantmentLevel = enchantmentLevel.updated(choice, newLevel))
          }
        }
      case None => IO.raiseError(new IllegalStateException("Level cannot be upgraded from maximum"))
    }
  }

  lazy val tryUpgradeByOneLevel: IO[MebiusProperty] = {
    for {
      levelUpHappened <- level.attemptLevelUp
      updatedProperty <- {
        if (levelUpHappened) {
          upgradeByOneLevel
        } else IO.pure {
          this
        }
      }
    } yield updatedProperty
  }

  lazy val ownerNickname: String = ownerNicknameOverride.getOrElse(ownerPlayerId)

  /**
   * `another` と異なる [[MebiusEnchantment]] を返す。
   */
  def enchantmentDifferentFrom(another: MebiusProperty): Option[MebiusEnchantment] =
    another.enchantmentLevel.keySet.union(enchantmentLevel.keySet)
      .find { e => another.enchantmentLevel.get(e) != enchantmentLevel.get(e) }
}

object MebiusProperty {
  def initialProperty(ownerPlayerId: String, ownerUuid: String): MebiusProperty = {
    MebiusProperty(
      ownerPlayerId,
      ownerUuid,
      enchantmentLevel = Map(
        MebiusEnchantment.Durability -> 3,
        MebiusEnchantment.Mending -> 1
      )
    )
  }
}
