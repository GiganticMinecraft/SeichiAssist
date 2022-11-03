package com.github.unchama.seichiassist.subsystems.gachaprize.subsystems.gachaticket.domain

case class GachaTicketAmount(value: Int) {
  require(value >= 0)
}
