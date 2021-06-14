package com.github.unchama.buildassist.enums

trait LineFillStatusFlag {
  val next: LineFillStatusFlag with Product
}

object LineFillStatusFlag {
  case object Disabled extends LineFillStatusFlag {
    override val next: LineFillStatusFlag with Product = UpperSide
  }

  case object UpperSide extends LineFillStatusFlag {
    override val next: LineFillStatusFlag with Product = LowerSide
  }

  case object LowerSide extends LineFillStatusFlag {
    override val next: LineFillStatusFlag with Product = Disabled
  }
}
