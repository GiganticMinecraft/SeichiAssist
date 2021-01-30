package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import org.scalacheck.Arbitrary

private[level] object LocalArbitrary {

  implicit val arbitrarySeichiExpAmount: Arbitrary[SeichiExpAmount] = Arbitrary {
    Arbitrary.arbBigDecimal.arbitrary.filter(_ >= 0).map(SeichiExpAmount.ofNonNegative)
  }

  implicit val arbitraryStarLevel: Arbitrary[SeichiStarLevel] = Arbitrary {
    Arbitrary.arbBigInt.arbitrary.filter(_ >= 0).map(SeichiStarLevel.ofNonNegative)
  }

}
