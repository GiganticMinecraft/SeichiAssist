package com.github.unchama.seichiassist.data.player
case class PlayerNickName(val style: Style = Style.SecondaryName, val id1: Int = 0, val id2: Int = 0, val id3: Int = 0) {
  enum class Style(val displayLevel: Boolean) {
    Level(true),
    SecondaryName(false),
    ;
  }

  object Style {
    def marshal(isLevel: Boolean): Style = {
      return if (isLevel) {
      Level
    } else {
      SecondaryName
    }
  }
}