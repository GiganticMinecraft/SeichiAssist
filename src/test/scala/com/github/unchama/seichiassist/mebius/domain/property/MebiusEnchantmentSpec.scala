package com.github.unchama.seichiassist.mebius.domain.property

import org.scalatest.wordspec.AnyWordSpec

class MebiusEnchantmentSpec extends AnyWordSpec {
  "Mebius enchantment" should {
    "be available to be upgraded for all levels upto maximum" in {
      (2 to MebiusLevel.max).foreach { level =>
        assert {
          val unlockableSkills = MebiusEnchantment.values.filter(_.unlockLevel.value <= level)

          unlockableSkills.map(_.maxLevel).sum >= level
        }
      }
    }
  }
}
