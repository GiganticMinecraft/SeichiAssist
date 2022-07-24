package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

case class FairySummonCost(value: Int) {
  require(0 < value && value < 5)
}
