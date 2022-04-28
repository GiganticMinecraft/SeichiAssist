package com.github.unchama.seichiassist.util

object ListFormatters {
  def getDescFormat(list: List[String]): String = s" ${list.mkString("", "\n", "\n")}"
}
