package com.github.unchama.seichiassist.subsystems.dragonnighttime.domain

import java.time.LocalTime
import scala.concurrent.duration._

case class Period(startAt: LocalTime, endAt: LocalTime) {
  require(startAt.isBefore(endAt))

  def contains(time: LocalTime): Boolean =
    startAt.isBefore(time) && time.isBefore(endAt)

  def toFiniteDuration: FiniteDuration =
    java.time.Duration.between(startAt, endAt).getSeconds.seconds

  def remainingDuration(time: LocalTime): Option[FiniteDuration] =
    Option.when(contains(time))(java.time.Duration.between(time, endAt).getSeconds.seconds)

  def toHours: Long = java.time.Duration.between(startAt, endAt).toHours
}
