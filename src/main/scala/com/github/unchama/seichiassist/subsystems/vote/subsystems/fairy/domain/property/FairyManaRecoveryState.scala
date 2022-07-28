package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairyManaRecoveryState

object FairyManaRecoveryState {

  case object full extends FairyManaRecoveryState

  case object notConsumptionApple extends FairyManaRecoveryState

  case object consumptionApple extends FairyManaRecoveryState

}
