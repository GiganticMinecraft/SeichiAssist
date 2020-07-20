package com.github.unchama.seichiassist.mebius.domain

class MebiusLevel private(val value: Int) extends AnyVal

object MebiusLevel {

  def apply(level: Int): MebiusLevel = {
    require(level >= 1)

    new MebiusLevel(level)
  }

}
