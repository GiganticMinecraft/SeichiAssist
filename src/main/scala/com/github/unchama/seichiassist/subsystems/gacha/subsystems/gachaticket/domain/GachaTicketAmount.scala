package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

case class GachaTicketAmount(value: Int) {
  require(value >= 0)
}
