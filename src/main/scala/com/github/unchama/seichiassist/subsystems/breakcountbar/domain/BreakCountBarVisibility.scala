package com.github.unchama.seichiassist.subsystems.breakcountbar.domain

sealed trait BreakCountBarVisibility {
  val nextValue: BreakCountBarVisibility
}

object BreakCountBarVisibility {

  case object ShownSeichiBreakAmount extends BreakCountBarVisibility {
    override lazy val nextValue: BreakCountBarVisibility = ShownBuildAmount
  }

  case object ShownBuildAmount extends BreakCountBarVisibility {
    override lazy val nextValue: BreakCountBarVisibility = ShownGiganticBerserkAmount
  }

  case object ShownGiganticBerserkAmount extends BreakCountBarVisibility {
    override lazy val nextValue: BreakCountBarVisibility = Hidden
  }

  case object Hidden extends BreakCountBarVisibility {
    override lazy val nextValue: BreakCountBarVisibility = ShownSeichiBreakAmount
  }

}
