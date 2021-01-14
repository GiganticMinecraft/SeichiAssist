package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SeichiLevelAndStarLevelSpec
  extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers {

  import LocalArbitrary._
  import cats.implicits._

  "SeichiLevel and StarLevel" should {
    "be continuous" in {
      val levelTable = SeichiLevelTable.table
      val starLevelTable = SeichiStarLevelTable

      forAll(minSuccessful(100000)) { seichiExpAmount: SeichiExpAmount =>
        (levelTable.levelAt(seichiExpAmount) < levelTable.maxLevel) shouldEqual
          (starLevelTable.levelAt(seichiExpAmount) == SeichiStarLevel.ofNonNegative(0))
      }
    }
  }

}
