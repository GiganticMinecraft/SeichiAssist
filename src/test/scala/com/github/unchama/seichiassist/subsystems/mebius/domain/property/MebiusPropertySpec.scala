package com.github.unchama.seichiassist.subsystems.mebius.domain.property

import cats.Monad
import cats.effect.SyncIO
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

class MebiusPropertySpec extends AnyWordSpec {
  val testPlayerName: String = "testPlayer"
  val testPlayerUuid: String = UUID.randomUUID().toString

  "Initial mebius property" should {
    "be valid" in {
      // exception thrown if invalid
      MebiusProperty.initialProperty(NormalMebius, testPlayerName, testPlayerUuid) equals
        MebiusProperty.initialProperty(NormalMebius, testPlayerName, testPlayerUuid)
    }

    "be able to be upgraded all the way to the maximum level" in {
      val upgradedToMaximum = {
        val initialProperty =
          MebiusProperty.initialProperty(NormalMebius, testPlayerName, testPlayerUuid)

        Monad[SyncIO]
          .iterateWhileM(initialProperty)(_.upgradeByOneLevel[SyncIO])(!_.level.isMaximum)
          .unsafeRunSync()
      }

      assert(upgradedToMaximum.level.isMaximum)
    }

    "not allow toggling materials" in {
      val i = MebiusProperty.initialProperty(NormalMebius, testPlayerName, testPlayerUuid)

      assert(i.forcedMaterial == i.toggleForcedMaterial.forcedMaterial)
    }
  }

  "Property with the largest level" should {
    "always contain Unbreakable enchantment" in {
      val upgradedToMaximum = {
        val initialProperty =
          MebiusProperty.initialProperty(NormalMebius, testPlayerName, testPlayerUuid)

        Monad[SyncIO]
          .iterateWhileM(initialProperty)(_.upgradeByOneLevel[SyncIO])(!_.level.isMaximum)
          .unsafeRunSync()
      }

      assert(upgradedToMaximum.enchantmentLevels.of(MebiusEnchantment.Unbreakable) == 1)
    }

    "allow toggling forced materials" in {
      val upgradedToMaximum = {
        val initialProperty =
          MebiusProperty.initialProperty(NormalMebius, testPlayerName, testPlayerUuid)

        Monad[SyncIO]
          .iterateWhileM(initialProperty)(_.upgradeByOneLevel[SyncIO])(!_.level.isMaximum)
          .unsafeRunSync()
      }

      assert(
        upgradedToMaximum
          .toggleForcedMaterial
          .forcedMaterial != upgradedToMaximum.forcedMaterial
      )
    }
  }

  "Levelling up" should {
    "upgrade exactly one enchantment at a time" in {
      val testIterationCount = 1000

      (1 to testIterationCount).foreach { _ =>
        val initialProperty =
          MebiusProperty.initialProperty(NormalMebius, testPlayerName, testPlayerUuid)

        import cats.implicits._

        Monad[SyncIO]
          .iterateWhileM(initialProperty) { property =>
            property.upgradeByOneLevel[SyncIO].flatTap { upgraded =>
              SyncIO {
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
