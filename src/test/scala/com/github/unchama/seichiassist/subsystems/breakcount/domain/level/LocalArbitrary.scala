package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import org.scalacheck.{Arbitrary, Gen}

private[level] object LocalArbitrary {

  implicit val arbitrarySeichiExpAmount: Arbitrary[SeichiExpAmount] = Arbitrary {
    // Longを使っているとはいえ、これより大きな整地量を入れると様々な箇所で表示エラー等が起こる
    // 1京ブロックとか整地する人が出てきたら考える
    Gen.choose(0, 1_0000_0000_0000_0000L).map(SeichiExpAmount.ofNonNegative)
  }

}
