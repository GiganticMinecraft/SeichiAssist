package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.util.enumeration.RelativeDirection

case class ClaimUnit(ahead: Int, behind: Int, right: Int, left: Int) {
  def apply(dt: RelativeDirection): Int = dt match {
    case RelativeDirection.AHEAD => ahead
    case RelativeDirection.BEHIND => behind
    case RelativeDirection.RIGHT => right
    case RelativeDirection.LEFT => left
  }
}