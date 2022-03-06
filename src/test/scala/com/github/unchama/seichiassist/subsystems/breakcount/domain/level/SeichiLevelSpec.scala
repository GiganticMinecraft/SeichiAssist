package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SeichiLevelSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  "SeichiLevel" should {
    "cap at 87115000" in {
      val cappingExp = 87115000

      assertResult(SeichiLevelTable.table.maxLevel) {
        SeichiLevelTable.table.levelAt(SeichiExpAmount.ofNonNegative(cappingExp))
      }

      assert(
        SeichiLevelTable.table.levelAt(SeichiExpAmount.ofNonNegative(cappingExp - 1))
          != SeichiLevelTable.table.maxLevel
      )
    }
  }
}
