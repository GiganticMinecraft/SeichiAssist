package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

case class FairySummonState(value: Int) {
  require(1 <= value && value <= 4, "FairySummonStateは1~4の間で指定してください。")
}
