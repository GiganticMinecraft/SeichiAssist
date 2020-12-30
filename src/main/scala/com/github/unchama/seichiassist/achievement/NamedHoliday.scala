package com.github.unchama.seichiassist.achievement

/**
 * 年によって日付が変わってしまうので、各年ごとに計算が必要な日の列挙
 */
sealed trait NamedHoliday

case object SpringEquinoxDay extends NamedHoliday
