package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

case class AppleAteByFairyRank(rank: Int) {
  require(rank >= 1)
}
