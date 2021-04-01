package com.github.unchama.buildassist.enums

trait LineFillStatusFlag

object LineFillStatusFlag {
  case object Disabled extends LineFillStatusFlag

  case object UpperSide extends LineFillStatusFlag

  case object LowerSide extends LineFillStatusFlag
}
