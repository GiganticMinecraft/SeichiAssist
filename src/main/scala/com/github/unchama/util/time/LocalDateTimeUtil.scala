package com.github.unchama.util.time

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.duration.FiniteDuration

object LocalDateTimeUtil {

  /**
   * t2からどれくらい経過すればt1になるかを計算する。 t1がt2より前であった場合、負の[[FiniteDuration]]が返る。
   */
  def difference(t1: LocalDateTime, t2: LocalDateTime): FiniteDuration = {
    import scala.concurrent.duration._
    (t1.toInstant(ZoneOffset.UTC).toEpochMilli - t2
      .toInstant(ZoneOffset.UTC)
      .toEpochMilli).milliseconds
  }
}
