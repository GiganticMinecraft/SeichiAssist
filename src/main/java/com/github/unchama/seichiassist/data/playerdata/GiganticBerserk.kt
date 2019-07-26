package com.github.unchama.seichiassist.data.playerdata

data class GiganticBerserk(val level: Int = 0, val exp: Int = 0, val stage: Int = 0, val canEvolution: Boolean = false, val cd: Int = 0) {
  fun reachedLimit(): Boolean {
    return stage == 4 && level == 9
  }

  fun regeneMadaProbability(): Double {
    val level = level
    return when {
      level < 2 -> 0.05
      level < 4 -> 0.06
      level < 6 -> 0.07
      level < 8 -> 0.08
      level < 9 -> 0.09
      else -> 0.10
    }
  }
}