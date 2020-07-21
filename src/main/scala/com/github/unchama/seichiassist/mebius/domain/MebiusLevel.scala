package com.github.unchama.seichiassist.mebius.domain

class MebiusLevel private(val value: Int) extends AnyVal {

  def isMaximum: Boolean = value == MebiusLevel.max

}

object MebiusLevel {

  val max = 30

  def apply(level: Int): MebiusLevel = {
    require(1 <= level && level < max)

    new MebiusLevel(level)
  }

}
