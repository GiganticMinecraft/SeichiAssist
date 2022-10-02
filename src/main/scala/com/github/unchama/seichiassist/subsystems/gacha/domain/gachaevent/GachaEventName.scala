package com.github.unchama.seichiassist.subsystems.gacha.domain.gachaevent

case class GachaEventName(name: String) {
  require(name != null)
}
