package com.github.unchama.seichiassist.mebius.service

import cats.effect.IO
import com.github.unchama.seichiassist.mebius.domain.MebiusProperty
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusEnchantments

object MebiusLevellingService {

  def attemptLevelUp(currentProperty: MebiusProperty): IO[MebiusProperty] = {
    for {
      levelUpAttempted <- currentProperty.level.attemptLevelUp
      updatedProperty <-
        if (levelUpAttempted != currentProperty.level) {
          currentProperty.incrementLevel.randomlyAugmentEnchantment(MebiusEnchantments.list.toSet)
        } else IO.pure {
          currentProperty
        }
    } yield updatedProperty
  }
}
