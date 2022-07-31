package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairySpawnRequestResult

object FairySpawnRequestResult {

  case object NotEnoughSeichiLevel extends FairySpawnRequestResult

  case object AlreadyFairySpawned extends FairySpawnRequestResult

  case object NotEnoughEffectPoint extends FairySpawnRequestResult

}
