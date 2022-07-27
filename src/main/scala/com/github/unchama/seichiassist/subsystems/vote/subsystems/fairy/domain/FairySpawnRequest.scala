package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairySpawnRequestResult,
  FairyUsingState
}

class FairySpawnRequest[F[_]: ConcurrentEffect, G[_], Player] {

  import cats.implicits._

  def spawnRequest(player: Player)(
    implicit breakCountAPI: BreakCountAPI[G, F, Player],
    fairyAPI: FairyAPI[F, Player],
    voteAPI: VoteAPI[F, Player]
  ): F[FairySpawnRequestResult] = for {
    seichiAmountDataRepository <- breakCountAPI.seichiAmountDataRepository(player).read
    usingState <- fairyAPI.fairyUsingState(player)
    effectPoints <- voteAPI.effectPoints(player)
    fairySummonCost <- fairyAPI.fairySummonCost(player)
  } yield {
    if (seichiAmountDataRepository.levelCorrespondingToExp.level < 10)
      FairySpawnRequestResult.NotEnoughSeichiLevel
    else if (usingState == FairyUsingState.Using) FairySpawnRequestResult.AlreadyFairySpawned
    else if (effectPoints.value < fairySummonCost.value * 2)
      FairySpawnRequestResult.NotEnoughEffectPoint
    else FairySpawnRequestResult.Success
  }

}
