package com.github.unchama.seichiassist.data.player

case class GiganticBerserk(level: Int = 0, exp: Int = 0, stage: Int = 0, canEvolve: Boolean = false, cd: Int = 0) {

  def reachedLimit(): Boolean = stage == 4 && level == 9

  def manaRegenerationProbability(): Double = {
    case level < 2 => 0.05
    case level < 4 => 0.06
    case level < 6 => 0.07
    case level < 8 => 0.08
    case level < 9 => 0.09
    case _ => 0.10
  }
}