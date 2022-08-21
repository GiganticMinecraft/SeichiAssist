package com.github.unchama.seichiassist.subsystems.awayscreenname.domain

case class IdleMinute(minute: Int) {
  require(minute >= 0, "IdleMinuteは0以上である必要があります。")

  /**
   * @return minuteを1だけ増加したものを返す
   */
  def increment: IdleMinute =
    this.copy(minute = minute + 1)

}

object IdleMinute {

  /**
   * IdleMinuteの初期値
   */
  val initial: IdleMinute = IdleMinute(0)

}
