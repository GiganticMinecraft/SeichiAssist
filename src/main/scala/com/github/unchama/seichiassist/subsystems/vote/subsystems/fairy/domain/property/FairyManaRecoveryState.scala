package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairyManaRecoveryState

object FairyManaRecoveryState {

  case object Full extends FairyManaRecoveryState

  case object NotConsumptionApple extends FairyManaRecoveryState

  case object ConsumptionApple extends FairyManaRecoveryState

}
