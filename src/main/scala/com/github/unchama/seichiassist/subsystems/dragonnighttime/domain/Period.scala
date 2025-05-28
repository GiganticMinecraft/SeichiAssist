package com.github.unchama.seichiassist.subsystems.dragonnighttime.domain

import java.time.LocalTime
import scala.concurrent.duration._

case class Period(startAt: LocalTime, endAt: LocalTime) {
  require(startAt.isBefore(endAt))

  def contains(time: LocalTime): Boolean =
    startAt.isBefore(time) && time.isBefore(time)

  def toFiniteDuration: FiniteDuration =
    java.time.Duration.between(startAt, endAt).getSeconds.seconds

  def remainingDuration(time: LocalTime): Option[FiniteDuration] =
    Option.when(contains(time))(java.time.Duration.between(time, endAt).getSeconds.seconds)

}

object Period {
  val effectivePeriod: Period = Period(LocalTime.of(20, 0, 0), LocalTime.of(21, 0, 0))
}
