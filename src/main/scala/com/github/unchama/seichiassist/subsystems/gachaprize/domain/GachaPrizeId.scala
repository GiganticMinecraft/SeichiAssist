package com.github.unchama.seichiassist.subsystems.gachaprize.domain

case class GachaPrizeId(id: Int) {
  require(id >= 0)
}
