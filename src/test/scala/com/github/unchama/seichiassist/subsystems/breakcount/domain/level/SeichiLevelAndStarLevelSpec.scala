package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SeichiLevelAndStarLevelSpec
    extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers {

  "SeichiLevel and StarLevel" should {
    "be continuous" in {
      val levelTable = SeichiLevelTable.table
      val starLevelTable = SeichiStarLevelTable

      assert {
        starLevelTable.levelAt(levelTable.expAt(levelTable.maxLevel)) != SeichiStarLevel.zero
      }
      assert {
        levelTable.expAt(levelTable.maxLevel) == starLevelTable.expAt(
          SeichiStarLevel.ofNonNegative(1)
        )
      }
    }
  }

}
