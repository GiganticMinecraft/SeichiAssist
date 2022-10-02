package com.github.unchama.seichiassist.subsystems.gacha.domain

case class GachaEventName(name: String) {
  require(name != null)
}
