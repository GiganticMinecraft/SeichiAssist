package com.github.unchama.seichiassist.data.player

case class PlayerNickName(style: Style = Style.SecondaryName,
                          id1: Int = 0,
                          id2: Int = 0,
                          id3: Int = 0)

object Style {

  case object Level extends Style(true)

  case object SecondaryName extends Style(false)

}

sealed abstract class Style(val displayLevel: Boolean) {

  def marshal(isLevel: Boolean): Style = if (isLevel) Style.Level else Style.SecondaryName

}