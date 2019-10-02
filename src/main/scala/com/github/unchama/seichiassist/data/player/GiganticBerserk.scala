package com.github.unchama.seichiassist.data.player

case class GiganticBerserk(level: Int = 0, exp: Int = 0, stage: Int = 0, canEvolve: Boolean = false, cd: Int = 0) {

  def reachedLimit(): Boolean = stage == 4 && level == 9

  def manaRegenerationProbability(): Double =
    if (level < 2) 0.05
    else if (level < 4) 0.06
    else if (level < 6) 0.07
    else if (level < 8) 0.08
    else if (level < 9) 0.09
    else 0.10
}