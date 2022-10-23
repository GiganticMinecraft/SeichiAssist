package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairySummonRequestError

object FairySummonRequestError {

  case object NotEnoughSeichiLevel extends FairySummonRequestError

  case object AlreadyFairySpawned extends FairySummonRequestError

  case object NotEnoughEffectPoint extends FairySummonRequestError

}
