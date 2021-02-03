package com.github.unchama.seichiassist.util.enumeration

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.auto._

sealed trait TimePeriodOfDay

object TimePeriodOfDay {
  case object Morning extends TimePeriodOfDay
  case object Day extends TimePeriodOfDay
  case object Night extends TimePeriodOfDay

  def apply(hour: Int Refined Positive): TimePeriodOfDay = {
    hour match {
      case _ if 4 <= hour && hour < 10 => Morning
      case _ if 10 <= hour && hour < 18 => Day
      case _ => Night
    }
  }
}