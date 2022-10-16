package com.github.unchama.seichiassist.util

import org.bukkit.Material
import org.scalatest.wordspec.AnyWordSpec

class BreakUtilSpec extends AnyWordSpec {
  "isAffectedByGravity" should {
    "check bedrock" in {
      assertResult(false)(BreakUtil.isAffectedByGravity(Material.BEDROCK))
    }
    "check fluid block" in {
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.WATER))
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.LAVA))
    }
    "check other non-solid block" in {
      assertResult(false)(BreakUtil.isAffectedByGravity(Material.SNOW))
      assertResult(false)(BreakUtil.isAffectedByGravity(Material.SUGAR_CANE_BLOCK))
      assertResult(false)(BreakUtil.isAffectedByGravity(Material.YELLOW_FLOWER))
      assertResult(false)(BreakUtil.isAffectedByGravity(Material.LONG_GRASS))
      assertResult(false)(BreakUtil.isAffectedByGravity(Material.AIR))
    }
    "check other solid block" in {
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.STONE))
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.COBBLESTONE))
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.FENCE))
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.GRASS))
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.NETHERRACK))
      assertResult(true)(BreakUtil.isAffectedByGravity(Material.ENDER_STONE))
    }
  }
}
