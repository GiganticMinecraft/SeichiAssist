package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.util.enumeration.DirectionType

case class ClaimUnit(ahead: Int, behind: Int, right: Int, left: Int) {
  def apply(dt: DirectionType): Int = dt match {
    case DirectionType.AHEAD => ahead
    case DirectionType.BEHIND => behind
    case DirectionType.RIGHT => right
    case DirectionType.LEFT => left
  }
}