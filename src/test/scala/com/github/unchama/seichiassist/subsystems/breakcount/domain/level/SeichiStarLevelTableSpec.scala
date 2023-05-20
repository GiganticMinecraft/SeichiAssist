package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SeichiStarLevelTableSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  import LocalArbitrary._
  import cats.implicits._

  "SeichiStarLevelTable" should {
    "satisfy the contract" in {
      import SeichiStarLevelTable.{expAt, levelAt}

      forAll { l: SeichiStarLevel => levelAt(expAt(l)) == l }
      forAll { e: SeichiExpAmount =>
        expAt(levelAt(e)) <= e && e <= expAt(levelAt(e).increment)
      }
    }
  }
}
