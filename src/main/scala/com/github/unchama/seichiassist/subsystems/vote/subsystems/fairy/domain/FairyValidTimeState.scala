package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

case class FairyValidTimeState(value: Int) {
  require(1 <= value && value <= 4, "FairySummonValidTimeStateは1~4の間で指定してください。")
}
