package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import java.time.LocalDateTime

case class FairySummonCost(value: Int) {
  require(1 <= value && value <= 4, "FairySummonCostは1~4の間で指定してください。")

  /**
   * @return 妖精が有効な時間
   * NOTE: 妖精が有効な時間はコストによって定められる。
   */
  def validTime: FairyValidTimes = {
    val now = LocalDateTime.now()
    val validTime = value match {
      case 1 =>
        FairyValidTimes(_, now.plusMonths(30))
      case 2 =>
        FairyValidTimes(_, now.plusHours(1))
      case 3 =>
        FairyValidTimes(_, now.plusHours(1).plusMinutes(30))
      case 4 =>
        FairyValidTimes(_, now.plusHours(2))
    }
    validTime(now)
  }

}
