package com.github.unchama.seichiassist.subsystems.breakcount.domain

sealed trait CardinalDirection

/**
 * 向いている方向
 */
object CardinalDirection {
  case object North extends CardinalDirection
  case object South extends CardinalDirection
  case object East extends CardinalDirection
  case object West extends CardinalDirection
  case object Up extends CardinalDirection
  case object Down extends CardinalDirection
}
