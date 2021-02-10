package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState

import java.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration

class FastDiggingEffectList(private val list: List[FastDiggingEffectTimings]) {
  def appendEffect(effect: FastDiggingEffect, duration: FiniteDuration)
                  (currentTime: LocalDateTime): FastDiggingEffectList = {
    val timings = FastDiggingEffectTimings(currentTime, duration, effect)

    new FastDiggingEffectList(list.appended(timings))
  }

  def filterInactive(currentTime: LocalDateTime): FastDiggingEffectList = {
    new FastDiggingEffectList(list.filter(_.isActiveAt(currentTime)))
  }

  def toFilteredList(currentTime: LocalDateTime): List[FastDiggingEffectTimings] = filterInactive(currentTime).list

  def totalEffectAmplifier(suppressionSettings: FastDiggingEffectSuppressionState)
                          (currentTime: LocalDateTime): Int = {
    val totalAmplifier: Int =
      toFilteredList(currentTime)
        .map(_.effect.amplifier)
        .sum
        .toInt

    (totalAmplifier - 1) min suppressionSettings.effectAmplifierCap
  }
}
