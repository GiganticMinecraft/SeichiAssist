package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairySpawnRequestError

object FairySpawnRequestError {

  case object NotEnoughSeichiLevel extends FairySpawnRequestError

  case object AlreadyFairySpawned extends FairySpawnRequestError

  case object NotEnoughEffectPoint extends FairySpawnRequestError

}
