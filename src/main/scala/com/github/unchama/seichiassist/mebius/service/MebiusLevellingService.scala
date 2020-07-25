package com.github.unchama.seichiassist.mebius.service

import cats.effect.IO
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusEnchantments

object MebiusLevellingService {

  def attemptLevelUp(currentProperty: MebiusProperty): IO[MebiusProperty] = {
    for {
      levelUpAttempted <- currentProperty.level.attemptLevelUp
      updatedProperty <-
        if (levelUpAttempted != currentProperty.level) {
          if (levelUpAttempted.isMaximum) IO.pure {
            // 最大レベルへの遷移ではunbreakableが付与されるため、追加でエンチャントを付与したくない
            currentProperty.incrementLevel
          } else {
            currentProperty.incrementLevel.randomlyUpgradeEnchantment(MebiusEnchantments.list.toSet)
          }
        } else IO.pure {
          currentProperty
        }
    } yield updatedProperty
  }
}
