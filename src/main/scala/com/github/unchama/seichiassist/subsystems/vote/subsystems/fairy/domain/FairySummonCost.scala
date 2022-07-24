package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

case class FairySummonCost(value: Int) {
  require(1 <= value && value <= 4, "FairySummonCostは1~4の間で指定してください。")
}
