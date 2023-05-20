package com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize

case class GachaPrizeId(id: Int) {
  require(id >= 0)
}
