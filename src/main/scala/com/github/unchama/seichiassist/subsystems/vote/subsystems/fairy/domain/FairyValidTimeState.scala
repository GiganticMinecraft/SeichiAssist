package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import java.time.LocalDateTime

case class FairyValidTimeState(value: Int) {
  require(1 <= value && value <= 4, "FairySummonValidTimeStateは1~4の間で指定してください。")

  /**
   * @return 妖精が有効な時間
   */
  def validTime: FairyValidTime = {
    val now = LocalDateTime.now()
    val validTime = value match {
      case 1 =>
        FairyValidTime(_, now.plusMonths(30))
      case 2 =>
        FairyValidTime(_, now.plusHours(1))
      case 3 =>
        FairyValidTime(_, now.plusHours(1).plusMinutes(30))
      case 4 =>
        FairyValidTime(_, now.plusHours(2))
    }
    validTime(now)
  }

}
