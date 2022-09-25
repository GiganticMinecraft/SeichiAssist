package com.github.unchama.seichiassist.subsystems.gacha.domain

case class GachaPrizeId(id: Int) {
  require(id >= 0)
}
