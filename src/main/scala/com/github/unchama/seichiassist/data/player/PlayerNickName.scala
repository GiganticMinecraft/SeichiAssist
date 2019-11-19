package com.github.unchama.seichiassist.data.player

sealed abstract class Style(val displayLevel: Boolean) {}

case class PlayerNickName(style: Style = PlayerNickName.Style.TitleCombination,
                          id1: Int = 0,
                          id2: Int = 0,
                          id3: Int = 0)

object PlayerNickName {

  object Style {

    def marshal(isLevel: Boolean): Style = if (isLevel) PlayerNickName.Style.Level else PlayerNickName.Style.TitleCombination

    case object Level extends Style(true)

    case object TitleCombination extends Style(false)

  }

}
