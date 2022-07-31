package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.ConcurrentEffect
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.domain.EffectPoint
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyRecoveryManaAmount
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

class BukkitSummonFairy[F[_]: ConcurrentEffect, G[_]: ContextCoercion[*[_], F]](
  implicit fairyAPI: FairyAPI[F, G, Player],
  voteAPI: VoteAPI[F, Player],
  manaApi: ManaApi[F, G, Player]
) extends SummonFairy[F, G, Player] {

  import cats.implicits._

  override def summon(player: Player): F[Unit] = {
    for {
      _ <- fairyAPI.updateIsFairyUsing(player, isFairyUsing = true)
      manaAmount <- ContextCoercion {
        manaApi.readManaAmount(player)
      }
      levelCappedManaAmount = manaAmount.cap.value
      recoveryManaAmount = FairyRecoveryManaAmount.manaAmountAt(levelCappedManaAmount)
      uuid = player.getUniqueId
      fairySummonCost <- fairyAPI.fairySummonCost(player)
      _ <- voteAPI.decreaseEffectPoint(uuid, EffectPoint(fairySummonCost.value * 2))
      _ <- fairyAPI.updateFairyRecoveryManaAmount(uuid, recoveryManaAmount)
      _ <- fairyAPI.updateFairyEndTime(player, fairySummonCost.endTime)
      _ <- new FairySpeech[F, G].summonSpeech(player)
      _ <- MessageEffectF(
        List(
          s"$RESET$YELLOW${BOLD}妖精を呼び出しました！",
          s"$RESET$YELLOW${BOLD}この子は1分間に約${recoveryManaAmount.recoveryMana}マナ",
          s"$RESET$YELLOW${BOLD}回復させる力を持っているようです。"
        )
      ).apply(player)
    } yield ()
  }

}
