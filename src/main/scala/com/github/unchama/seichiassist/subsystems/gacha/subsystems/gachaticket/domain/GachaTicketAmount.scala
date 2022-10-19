package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

case class GachaTicketAmount(amount: Int) {
  require(amount >= 0)
}
