package com.github.unchama.seichiassist.mebius.domain

class MebiusLevel private(val value: Int) extends AnyVal

object MebiusLevel {

  val max = 30

  def apply(level: Int): MebiusLevel = {
    require(1 <= level && level < max)

    new MebiusLevel(level)
  }

}
