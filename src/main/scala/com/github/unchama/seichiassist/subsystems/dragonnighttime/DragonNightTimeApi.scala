package com.github.unchama.seichiassist.subsystems.dragonnighttime

import java.time.LocalDateTime

trait DragonNightTimeApi {
  def isInDragonNightTime(dateTime: LocalDateTime): Boolean
}
