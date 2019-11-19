package com.github.unchama.seichiassist.data.player

sealed trait NicknameStyle
object NicknameStyle {
  case object Level extends NicknameStyle
  case object TitleCombination extends NicknameStyle

  def marshal(isLevel: Boolean): NicknameStyle = if (isLevel) Level else TitleCombination
}

case class PlayerNickname(style: NicknameStyle = NicknameStyle.TitleCombination,
                          id1: Int = 0, id2: Int = 0, id3: Int = 0)
