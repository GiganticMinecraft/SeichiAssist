package com.github.unchama.seichiassist.mebius.bukkit.codec

import com.github.unchama.seichiassist.subsystems.mebius.domain.property.MebiusLevel
import org.bukkit.Material
import org.scalatest.wordspec.AnyWordSpec

class BukkitMebiusAppearanceMaterialCodecSpec extends AnyWordSpec {

  import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusAppearanceMaterialCodec._

  "Appearance Codec" should {
    "return some non-air material for all levels" in {
      (1 until MebiusLevel.max.value)
        .map(MebiusLevel.apply)
        .foreach { mebiusLevel =>
          assert(appearanceMaterialAt(mebiusLevel) != Material.AIR)
        }
    }

    "assign leather helmet to level 1 mebius" in {
      assert {
        appearanceMaterialAt(MebiusLevel(1)) == Material.LEATHER_HELMET
      }
    }

    "assign diamond helmet to the maximum leven mebius" in {
      assert {
        appearanceMaterialAt(MebiusLevel.max) == Material.DIAMOND_HELMET
      }
    }
  }

}
