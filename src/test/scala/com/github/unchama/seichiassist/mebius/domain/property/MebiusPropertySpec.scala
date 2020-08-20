package com.github.unchama.seichiassist.mebius.domain.property

import java.util.UUID

import cats.Monad
import cats.effect.IO
import org.scalatest.wordspec.AnyWordSpec

class MebiusPropertySpec extends AnyWordSpec {
  "Initial mebius property" should {
    val testPlayerName = "testPlayer"
    val testPlayerUuid = UUID.randomUUID().toString

    "be valid" in {
      // exception thrown if invalid
      MebiusProperty.initialProperty(testPlayerName, testPlayerUuid) equals
        MebiusProperty.initialProperty(testPlayerName, testPlayerUuid)
    }

    "be able to be upgraded all the way to the maximum level" in {
      val upgradedToMaximum = {
        Monad[IO]
          .iterateWhileM {
            MebiusProperty.initialProperty(testPlayerName, testPlayerUuid)
          } {
            _.incrementLevel.randomlyUpgradeEnchantment
          } {
            !_.level.isMaximum
          }.unsafeRunSync()
      }

      assert(upgradedToMaximum.level.isMaximum)
    }
  }
}
