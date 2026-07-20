package com.github.unchama.seichiassist.subsystems.breakcountbar.domain

sealed trait BreakCountBarVisibility {
  def nextValue: BreakCountBarVisibility
}

object BreakCountBarVisibility {

  case object Shown extends BreakCountBarVisibility {
    override lazy val nextValue: BreakCountBarVisibility = Hidden
  }

  case object Hidden extends BreakCountBarVisibility {
    override lazy val nextValue: BreakCountBarVisibility = Shown
  }

}
