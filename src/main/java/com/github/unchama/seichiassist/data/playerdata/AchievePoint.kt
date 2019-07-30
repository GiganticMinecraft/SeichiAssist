package com.github.unchama.seichiassist.data.playerdata

data class AchievePoint(val totallyGet: Int = 0, val used: Int = 0, val convertCount: Int = 0) {
  // いちいち変更するのは冗長
  val left = totallyGet + convertCount * 3 - used
}