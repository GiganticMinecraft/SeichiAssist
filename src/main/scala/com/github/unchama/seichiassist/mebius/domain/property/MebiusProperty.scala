package com.github.unchama.seichiassist.mebius.domain.property

import cats.effect.Sync

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

  import cats.implicits._

  def upgradeByOneLevel[F[_]](implicit F: Sync[F]): F[MebiusProperty] = {
    level.increment match {
      case Some(newMebiusLevel) =>
        val upgradeEnchantmentLevels =
          if (newMebiusLevel.isMaximum) F.pure {
            enchantmentLevels.addNew(MebiusEnchantment.Unbreakable)
          } else {
            enchantmentLevels.randomlyUpgradeAt[F](newMebiusLevel)
          }

        upgradeEnchantmentLevels.map { upgradedEnchantmentLevels =>
          this.copy(
            level = newMebiusLevel,
            enchantmentLevels = upgradedEnchantmentLevels
          )
        }
      case None =>
        F.raiseError(new IllegalStateException("Level cannot be upgraded from maximum"))
    }
  }

  def tryUpgradeByOneLevel[F[_] : Sync]: F[MebiusProperty] = {
    for {
      levelUpHappened <- level.attemptLevelUp[F]
      updatedProperty <- {
        if (levelUpHappened) {
          upgradeByOneLevel
        } else {
          Sync[F].pure(this)
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
