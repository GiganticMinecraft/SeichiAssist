package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit

import cats.effect.Sync
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairySummonRequestError
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyPersistence,
  FairySpawnRequestErrorOrSpawn,
  FairySummonRequest
}
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import org.bukkit.entity.Player

class BukkitFairySummonRequest[F[_]: Sync, G[_]: ContextCoercion[*[_], F]](
  implicit breakCountAPI: BreakCountAPI[F, G, Player],
  voteAPI: VoteAPI[F, Player],
  fairyPersistence: FairyPersistence[F],
  summonFairy: SummonFairy[F, Player]
) extends FairySummonRequest[F, Player] {

//  import cats.implicits._

  override def summonRequest(player: Player): F[FairySpawnRequestErrorOrSpawn[F]] = {
//    val uuid = player.getUniqueId
//    for {
//      usingState <- fairyPersistence.isFairyUsing(uuid)
//      effectPoints <- voteAPI.effectPoints(player)
//      fairySummonCost <- fairyPersistence.fairySummonCost(uuid)
//      seichiAmountRepository <- ContextCoercion(
//        breakCountAPI.seichiAmountDataRepository(player).read
//      )
//      seichiLevel = seichiAmountRepository.levelCorrespondingToExp.level
//    } yield {
//      if (seichiLevel < 10)
//        Left(FairySummonRequestError.NotEnoughSeichiLevel)
//      else if (usingState)
//        Left(FairySummonRequestError.AlreadyFairySummoned)
//      else if (effectPoints.value < fairySummonCost.value * 2)
//        Left(FairySummonRequestError.NotEnoughEffectPoint)
//      else
//        Right(summonFairy.summon(player))
//    }
    Sync[F].pure(Right(MessageEffectF[F]("マナ妖精機能は不具合のため一時停止中です。").apply(player)))
  }

}
