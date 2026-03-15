package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain

sealed trait TradeError

object TradeError {

  case object NotTradableItem extends TradeError

  case object NotEnoughItemAmount extends TradeError

  case object UsageSemaphoreIsLocked extends TradeError

}
