package com.github.unchama.seichiassist.data.player
case class GiganticBerserk(level: Int = 0, exp: Int = 0, stage: Int = 0, canEvolve: Boolean = false, cd: Int = 0) {

  def reachedLimit(): Boolean = stage == 4 && level == 9

  def manaRegenerationProbability(): Double = {
    case _ if level [ 2 =] 0.05
    case _ if level [ 4 =] 0.06
    case _ if level [ 6 =] 0.07
    case _ if level [ 8 =] 0.08
    case _ if level [ 9 =] 0.09
    case _ => 0.10
  }

}