package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.Sync
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.domain.EffectPoint
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyRecoveryManaAmount
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

class BukkitSummonFairy[F[_]: Sync, G[_]: ContextCoercion[*[_], F]](
  implicit voteAPI: VoteAPI[F, Player],
  manaApi: ManaApi[F, G, Player],
  fairyPersistence: FairyPersistence[F],
  fairySpeech: FairySpeech[F, Player]
) extends SummonFairy[F, Player] {

  import cats.implicits._

  override def summon(player: Player): F[Unit] = {
    val uuid = player.getUniqueId
    for {
      _ <- fairyPersistence.updateIsFairyUsing(uuid, isFairyUsing = true)
      manaAmount <- ContextCoercion {
        manaApi.readManaAmount(player)
      }
      levelCappedManaAmount = manaAmount.cap.value
      recoveryManaAmount = FairyRecoveryManaAmount.manaAmountAt(levelCappedManaAmount)
      uuid = player.getUniqueId
      fairySummonCost <- fairyPersistence.fairySummonCost(uuid)
      _ <- voteAPI.decreaseEffectPoint(uuid, EffectPoint(fairySummonCost.value * 2))
      _ <- fairyPersistence.updateFairyRecoveryMana(uuid, recoveryManaAmount)
      _ <- fairyPersistence.updateFairyEndTime(uuid, fairySummonCost.endTime)
      _ <- fairySpeech.summonSpeech(player)
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
