package com.github.unchama.seichiassist.mebius.domain.property

import cats.effect.IO

/**
 * @param ownerPlayerId         オーナーのプレーヤーID
 * @param ownerUuid             オーナーのUUID文字列
 * @param enchantmentLevels     付与されるエンチャントのレベル
 * @param level                 Mebiusのレベル
 * @param ownerNicknameOverride オーナーをMebiusがどう呼ぶか
 * @param mebiusName            Mebius自体の名前
 */
case class MebiusProperty private(ownerPlayerId: String,
                                  ownerUuid: String,
                                  enchantmentLevels: MebiusEnchantmentLevels,
                                  level: MebiusLevel = MebiusLevel(1),
                                  ownerNicknameOverride: Option[String] = None,
                                  mebiusName: String = "MEBIUS") {

  require(enchantmentLevels.isValidAt(level))

  lazy val upgradeByOneLevel: IO[MebiusProperty] = {
    level.increment match {
      case Some(newMebiusLevel) =>
        val upgradeEnchantmentLevels =
          if (newMebiusLevel.isMaximum) IO.pure {
            enchantmentLevels.addNew(MebiusEnchantment.Unbreakable)
          } else {
            enchantmentLevels.randomlyUpgradeAt[IO](newMebiusLevel)
          }

        upgradeEnchantmentLevels.map { upgradeEnchantmnetLevels =>
          this.copy(
            level = newMebiusLevel,
            enchantmentLevels = upgradeEnchantmnetLevels
          )
        }
      case None =>
        IO.raiseError(new IllegalStateException("Level cannot be upgraded from maximum"))
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

}

object MebiusProperty {
  def initialProperty(ownerPlayerId: String, ownerUuid: String): MebiusProperty = {
    MebiusProperty(
      ownerPlayerId,
      ownerUuid,
      enchantmentLevels = MebiusEnchantmentLevels(
        MebiusEnchantment.Durability -> 3,
        MebiusEnchantment.Mending -> 1
      )
    )
  }
}
