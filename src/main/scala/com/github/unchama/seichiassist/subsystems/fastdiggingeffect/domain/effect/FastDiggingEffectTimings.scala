package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import com.github.unchama.util.time.LocalDateTimeUtil

import java.time.LocalDateTime
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * 採掘速度上昇効果の付与時刻と効果時間をセットで持つデータ型。
 */
case class FastDiggingEffectTimings(
  givenAt: LocalDateTime,
  totalDuration: FiniteDuration,
  effect: FastDiggingEffect
) {

  def isActiveAt(time: LocalDateTime): Boolean = {
    val pastTime = LocalDateTimeUtil.difference(time, givenAt)

    0.seconds <= pastTime && pastTime <= totalDuration
  }
}
