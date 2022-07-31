package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import cats.effect.Sync
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairySpawnRequestError

class FairySpawnRequest[F[_]: Sync, G[_]: ContextCoercion[*[_], F], Player](
  implicit breakCountAPI: BreakCountAPI[F, G, Player],
  fairyAPI: FairyAPI[F, G, Player],
  voteAPI: VoteAPI[F, Player],
  summonFairy: SummonFairy[F, G, Player]
) {

  import cats.implicits._

  def spawnRequest(player: Player): F[Either[FairySpawnRequestError, F[Unit]]] = {
    for {
      usingState <- fairyAPI.isFairyUsing(player)
      effectPoints <- voteAPI.effectPoints(player)
      fairySummonCost <- fairyAPI.fairySummonCost(player)
      seichiAmountRepository <- ContextCoercion(
        breakCountAPI.seichiAmountDataRepository(player).read
      )
      seichiLevel = seichiAmountRepository.levelCorrespondingToExp.level
    } yield {
      if (seichiLevel < 10)
        Left(FairySpawnRequestError.NotEnoughSeichiLevel)
      else if (usingState)
        Left(FairySpawnRequestError.AlreadyFairySpawned)
      else if (effectPoints.value < fairySummonCost.value * 2)
        Left(FairySpawnRequestError.NotEnoughEffectPoint)
      else
        Right(summonFairy.summon(player))
    }
  }

}
