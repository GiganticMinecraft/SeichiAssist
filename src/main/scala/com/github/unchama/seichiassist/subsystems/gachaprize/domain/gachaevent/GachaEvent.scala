package com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent

import java.time.LocalDate

case class GachaEvent(eventName: GachaEventName, startDate: LocalDate, endDate: LocalDate) {
  require(startDate.isBefore(endDate))
}
