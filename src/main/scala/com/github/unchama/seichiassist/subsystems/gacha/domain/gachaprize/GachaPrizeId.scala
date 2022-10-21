package com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize

case class GachaPrizeId(id: Int) {
  require(id >= 0)
}
