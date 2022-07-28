package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairySpawnRequestResult,
  FairyUsingState
}

class FairySpawnRequest[F[_]: ConcurrentEffect, G[_]: ContextCoercion[*[_], F], Player] {

  import cats.implicits._

  def spawnRequest(player: Player)(
    implicit breakCountAPI: BreakCountAPI[F, G, Player],
    fairyAPI: FairyAPI[F, G, Player],
    voteAPI: VoteAPI[F, Player]
  ): F[FairySpawnRequestResult] = {
    val seichiAmountRepository = ContextCoercion(
      breakCountAPI.seichiAmountDataRepository(player).read
    ).toIO.unsafeRunSync().levelCorrespondingToExp.level

    for {
      usingState <- fairyAPI.fairyUsingState(player)
      effectPoints <- voteAPI.effectPoints(player)
      fairySummonCost <- fairyAPI.fairySummonCost(player)
    } yield {
      if (seichiAmountRepository < 10)
        FairySpawnRequestResult.NotEnoughSeichiLevel
      else if (usingState == FairyUsingState.Using) FairySpawnRequestResult.AlreadyFairySpawned
      else if (effectPoints.value < fairySummonCost.value * 2)
        FairySpawnRequestResult.NotEnoughEffectPoint
      else FairySpawnRequestResult.Success
    }
  }

}
