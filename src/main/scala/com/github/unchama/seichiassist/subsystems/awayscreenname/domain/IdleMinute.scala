package com.github.unchama.seichiassist.subsystems.awayscreenname.domain

case class IdleMinute(minute: Int) {
  require(minute >= 0, "IdleMinuteは0以上である必要があります。")
}
