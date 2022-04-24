package com.github.unchama.seichiassist.util

import java.text.SimpleDateFormat
import java.util.Calendar

object TimeUtil {
  def showTime(cal: Calendar): String = {
    val date = cal.getTime
    val format = new SimpleDateFormat("yyyy/MM/dd HH:mm")
    format.format(date)
  }

  def showHour(cal: Calendar): String = {
    val date = cal.getTime
    val format = new SimpleDateFormat("HH:mm")
    format.format(date)
  }

  def getTimeZone(cal: Calendar): String = {
    val date = cal.getTime
    val format = new SimpleDateFormat("HH")
    val n = Integer.parseInt(format.format(date))
    if (4 <= n && n < 10)
      "morning"
    else if (10 <= n && n < 18)
      "day"
    else
      "night"
  }
}
