package com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent

case class GachaEventName(name: String) {
  require(name != null && name.length <= 30)
}
