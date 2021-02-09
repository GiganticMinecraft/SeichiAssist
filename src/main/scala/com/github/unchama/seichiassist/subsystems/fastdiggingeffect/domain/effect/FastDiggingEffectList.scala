package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect

import java.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration

class FastDiggingEffectList(list: List[FastDiggingEffectTimings]) {

  def appendEffect(effect: FastDiggingEffect, duration: FiniteDuration)
                  (currentTime: LocalDateTime): FastDiggingEffectList = {
    val timings = FastDiggingEffectTimings(currentTime, duration, effect)

    new FastDiggingEffectList(list.appended(timings))
  }

  def filterInactive(currentTime: LocalDateTime): FastDiggingEffectList = {
    new FastDiggingEffectList(list.filter(_.isActiveAt(currentTime)))
  }

  def toFilteredList(currentTime: LocalDateTime): List[FastDiggingEffectTimings] = filterInactive(currentTime).list

}
