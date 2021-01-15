package com.github.unchama.seichiassist.subsystems.breakcountbar.domain

sealed trait BreakCountBarVisibility

object BreakCountBarVisibility {

  case object Shown extends BreakCountBarVisibility

  case object Hidden extends BreakCountBarVisibility

}
