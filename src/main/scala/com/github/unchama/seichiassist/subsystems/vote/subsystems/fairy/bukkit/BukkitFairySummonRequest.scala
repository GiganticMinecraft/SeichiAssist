package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.Sync
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairySummonRequest,
  FairySpawnRequestErrorOrSpawn
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairySpawnRequestError

class BukkitFairySummonRequest[F[_]: Sync, G[_]: ContextCoercion[*[_], F], Player](
  implicit breakCountAPI: BreakCountAPI[F, G, Player],
  fairyAPI: FairyAPI[F, G, Player],
  voteAPI: VoteAPI[F, Player]
) extends FairySummonRequest[F, Player] {

  import cats.implicits._

  override def summonRequest(player: Player): F[FairySpawnRequestErrorOrSpawn[F]] = {
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
        Right(fairyAPI.fairySummon(player))
    }
  }

}
