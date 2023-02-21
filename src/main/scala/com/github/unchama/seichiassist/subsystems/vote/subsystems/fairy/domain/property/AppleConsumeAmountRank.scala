package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

case class AppleConsumeAmountRank(playerName: String, rank: Int, consumed: AppleAmount) {
  require(rank >= 1, "rankは1以上の値で指定してください。")
}
