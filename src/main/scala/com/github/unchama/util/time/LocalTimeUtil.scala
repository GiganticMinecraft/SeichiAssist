package com.github.unchama.util.time

import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration

object LocalTimeUtil {

  /**
   * t2からどれくらい経過すればt1になるかを計算する。 t1がt2より前であった場合、負の[[FiniteDuration]]が返る。
   */
  def difference(t1: LocalTime, t2: LocalTime): FiniteDuration = {
    import scala.concurrent.duration._
    (t1.toNanoOfDay - t2.toNanoOfDay).nanoseconds
  }

  /**
   * 現在時刻`current`から次の時刻`fixedTime`までの[[FiniteDuration]]を計算する。
   */
  def getDurationToNextTimeOfDay(current: LocalTime, fixedTime: LocalTime): FiniteDuration = {
    import scala.concurrent.duration._

    if (current.isBefore(fixedTime)) {
      difference(fixedTime, current)
    } else {
      1.day - difference(current, fixedTime)
    }
  }
}
