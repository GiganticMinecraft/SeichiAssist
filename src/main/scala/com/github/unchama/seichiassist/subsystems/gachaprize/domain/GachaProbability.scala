package com.github.unchama.seichiassist.subsystems.gachaprize.domain

case class GachaProbability(value: Double) {
  require(value >= 0.0 && value <= 1.0)
}
