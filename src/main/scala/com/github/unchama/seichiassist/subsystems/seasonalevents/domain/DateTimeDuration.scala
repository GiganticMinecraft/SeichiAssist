package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import java.time.{LocalDate, LocalDateTime, LocalTime}

/**
 * 2つの[[LocalDateTime]]で表される期間を表す
 *
 * @param from
 *   期間の開始日
 * @param to
 *   期間の終了日
 * @see
 *   [[LocalTime]]を省略するならば、`DateTimeDuration.fromLocalDate`を使用する
 * @throws IllegalArgumentException
 *   `from`が`to`より後である（等しいときはthrowされない）
 */
case class DateTimeDuration(from: LocalDateTime, to: LocalDateTime) {
  require(from.isBefore(to) || from.isEqual(to), "期間の開始日が終了日よりも後に指定されています。")

  /**
   * 指定した[[LocalDateTime]]が`DateTimeDuration.from`と`DateTimeDuration.to`の期間内にあるかどうか
   *
   * @param base
   *   比較する[[LocalDateTime]]
   * @return
   *   以下の条件をどちらも満たせば`true`
   *   - 指定した[[LocalDateTime]]が、`DateTimeDuration.from`より前にあるもしくは等しい
   *   - 指定した[[LocalDateTime]]が、`DateTimeDuration.from`より後にあるもしくは等しい
   */
  def contains(base: LocalDateTime): Boolean = {
    val isAfterFrom = base.isEqual(from) || base.isAfter(from)
    val isBeforeTo = base.isEqual(to) || base.isBefore(to)

    isAfterFrom && isBeforeTo
  }

  /**
   * 指定した[[LocalDateTime]]が`DateTimeDuration.from`より前にあるかどうか
   *
   * @param base
   *   比較する[[LocalDateTime]]
   * @return
   *   指定した[[LocalDateTime]]が、`DateTimeDuration.from`より前にあるもしくは等しければ`true`
   */
  def isEntirelyAfter(base: LocalDateTime): Boolean = base.isBefore(from) || base.isEqual(from)
}

object DateTimeDuration {
  private val REBOOT_TIME = LocalTime.of(4, 10)

  /**
   * [[LocalDate]]から[[DateTimeDuration]]を生成する
   *
   * @param from
   *   期間の開始日
   * @param to
   *   期間の終了日
   * @return
   *   受け取った`from`と`to`に[[REBOOT_TIME]]の時刻をつけた2つの[[LocalDateTime]]を持つ[[DateTimeDuration]]
   */
  def fromLocalDate(from: LocalDate, to: LocalDate): DateTimeDuration =
    DateTimeDuration(LocalDateTime.of(from, REBOOT_TIME), LocalDateTime.of(to, REBOOT_TIME))
}
