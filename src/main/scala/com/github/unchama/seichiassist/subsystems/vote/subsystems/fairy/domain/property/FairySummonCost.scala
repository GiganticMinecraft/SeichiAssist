package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import java.time.LocalDateTime

case class FairySummonCost(value: Int) {
  require(1 <= value && value <= 4, "FairySummonCostは1~4の間で指定してください。")

  /**
   * @return 妖精の効果が終了する時間
   * NOTE: 妖精の効果が終了する時間はコストによって定められる。
   */
  def endTime: FairyEndTime = {
    val now = LocalDateTime.now()
    val validTime = value match {
      case 1 =>
        FairyEndTime(Some(now.plusMinutes(30)))
      case 2 =>
        FairyEndTime(Some(now.plusHours(1)))
      case 3 =>
        FairyEndTime(Some(now.plusHours(1).plusMinutes(30)))
      case 4 =>
        FairyEndTime(Some(now.plusHours(2)))
    }
    validTime
  }

  def finiteDuration: String = {
    value match {
      case 1 => "30分"
      case 2 => "1時間"
      case 3 => "1時間30分"
      case 4 => "2時間"
    }
  }

}
