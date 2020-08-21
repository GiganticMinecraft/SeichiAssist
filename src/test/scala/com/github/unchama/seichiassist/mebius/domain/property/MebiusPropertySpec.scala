package com.github.unchama.seichiassist.mebius.domain.property

import java.util.UUID

import cats.Monad
import cats.effect.IO
import org.scalatest.wordspec.AnyWordSpec

class MebiusPropertySpec extends AnyWordSpec {
  val testPlayerName: String = "testPlayer"
  val testPlayerUuid: String = UUID.randomUUID().toString

  "Initial mebius property" should {
    "be valid" in {
      // exception thrown if invalid
      MebiusProperty.initialProperty(testPlayerName, testPlayerUuid) equals
        MebiusProperty.initialProperty(testPlayerName, testPlayerUuid)
    }

    "be able to be upgraded all the way to the maximum level" in {
      val upgradedToMaximum = {
        val initialProperty = MebiusProperty.initialProperty(testPlayerName, testPlayerUuid)

        Monad[IO]
          .iterateWhileM(initialProperty)(_.upgradeByOneLevel)(!_.level.isMaximum)
          .unsafeRunSync()
      }

      assert(upgradedToMaximum.level.isMaximum)
    }
  }

  "Property with the largest level" should {
    "always contain Unbreakable enchantment" in {
      val upgradedToMaximum = {
        val initialProperty = MebiusProperty.initialProperty(testPlayerName, testPlayerUuid)

        Monad[IO]
          .iterateWhileM(initialProperty)(_.upgradeByOneLevel)(!_.level.isMaximum)
          .unsafeRunSync()
      }

      assert(upgradedToMaximum.enchantmentLevels.of(MebiusEnchantment.Unbreakable) == 1)
    }
  }

  "Levelling up" should {
    "upgrade exactly one enchantment at a time" in {
      val testIterationCount = 1000

      (1 to testIterationCount).foreach { _ =>
        val initialProperty = MebiusProperty.initialProperty(testPlayerName, testPlayerUuid)

        import cats.implicits._

        Monad[IO]
          .iterateWhileM(initialProperty) { property =>
            property.upgradeByOneLevel.flatTap { upgraded =>
              IO {
                val oldLevels = property.enchantmentLevels
                val newLevels = upgraded.enchantmentLevels

                val diff = newLevels.differenceFrom(oldLevels)

                assert(diff.size == 1)
                assert(newLevels.of(diff.head) == oldLevels.of(diff.head) + 1)
              }
            }
          }(!_.level.isMaximum)
          .unsafeRunSync()
      }
    }
  }
}
