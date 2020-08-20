package com.github.unchama.seichiassist.mebius.service

import cats.effect.IO
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty

object MebiusLevellingService {

  def attemptLevelUp(currentProperty: MebiusProperty): IO[MebiusProperty] = {
    for {
      levelUpHappened <- currentProperty.level.attemptLevelUp
      updatedProperty <- {
        if (levelUpHappened) {
          currentProperty.upgradeByOneLevel
        } else IO.pure {
          currentProperty
        }
      }
    } yield updatedProperty
  }
}
