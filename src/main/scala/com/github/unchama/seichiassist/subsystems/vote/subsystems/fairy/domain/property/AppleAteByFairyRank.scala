package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

case class AppleAteByFairyRank(name: String, rank: Int, appleAmount: AppleAmount) {
  require(rank >= 1)
}
