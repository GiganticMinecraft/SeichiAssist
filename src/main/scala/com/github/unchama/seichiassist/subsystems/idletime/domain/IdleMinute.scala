package com.github.unchama.seichiassist.subsystems.idletime.domain

case class IdleMinute(minutes: Int) {
  require(minutes >= 0, "IdleMinuteは0以上である必要があります。")

  /**
   * @return minuteを1だけ増加したものを返す
   */
  def increment: IdleMinute =
    this.copy(minutes = minutes + 1)

}

object IdleMinute {

  /**
   * [[IdleMinute]]の初期値
   */
  val initial: IdleMinute = IdleMinute(0)

}
