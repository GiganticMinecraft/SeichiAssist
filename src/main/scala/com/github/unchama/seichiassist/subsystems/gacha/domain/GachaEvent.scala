package com.github.unchama.seichiassist.subsystems.gacha.domain

import java.time.LocalDateTime

case class GachaEvent(eventName: String, startTime: LocalDateTime, endTime: LocalDateTime) {
  require(eventName != null && startTime.isBefore(endTime))
}
