package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, LiftIO, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.domain.EffectPoint
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairyUsingState.Using
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyRecoveryManaAmount,
  FairyUsingState
}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

object SummonFairy {

  import cats.implicits._

  def apply[F[_]: ConcurrentEffect: LiftIO, G[_]: SyncEffect: ContextCoercion[*[_], F]](
    player: Player
  )(
    implicit breakCountAPI: BreakCountAPI[F, G, Player],
    fairyAPI: FairyAPI[F],
    voteAPI: VoteAPI[F],
    manaApi: ManaApi[F, G, Player]
  ): SummonFairy[F] = new SummonFairy[F] {
    override def summon: F[Unit] = {
      val playerLevel =
        ContextCoercion(breakCountAPI.seichiAmountDataRepository[G](player).read.map {
          _.levelCorrespondingToExp.level
        }).toIO.unsafeRunSync()

      val notEnoughLevelEffect =
        SequentialEffect(
          MessageEffect(s"${GOLD}プレイヤーレベルが足りません"),
          FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
        )
      val alreadySummoned =
        SequentialEffect(
          MessageEffect(s"${GOLD}既に妖精を召喚しています"),
          FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
        )
      val effectPoint =
        SequentialEffect(
          MessageEffect(s"${GOLD}投票ptが足りません"),
          FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
        )

      val uuid = player.getUniqueId

      val validTimeState = fairyAPI.fairyValidTimeState(uuid).toIO.unsafeRunSync()

      LiftIO[F].liftIO {
        if (playerLevel < 10) notEnoughLevelEffect(player) // レベル不足
        else if (fairyAPI.fairyUsingState(uuid).toIO.unsafeRunSync() == Using)
          alreadySummoned(player) // 既に召喚している
        else if (
          voteAPI.effectPoints(uuid).toIO.unsafeRunSync().value < validTimeState.value * 2
        )
          effectPoint(player) // 投票ptがたりなかった
        else {
          val levelCappedManaAmount =
            ContextCoercion(manaApi.readManaAmount(player)).toIO.unsafeRunSync().cap.value

          // 回復するマナの量
          val recoveryMana = FairyRecoveryManaAmount.manaAmountAt(levelCappedManaAmount)

          import cats.implicits._

          val eff = for {
            _ <- fairyAPI.updateFairyUsingState(uuid, FairyUsingState.Using)
            _ <- voteAPI.decreaseEffectPoint(uuid, EffectPoint(validTimeState.value * 2))
            _ <- fairyAPI.updateFairyRecoveryManaAmount(uuid, recoveryMana)
            _ <- fairyAPI.updateFairyValidTimes(uuid, Some(validTimeState.validTime))
          } yield ()

          SequentialEffect(
            UnfocusedEffect(eff.toIO.unsafeRunAsyncAndForget()),
            MessageEffect(
              List(
                s"$RESET$YELLOW${BOLD}妖精を呼び出しました！",
                s"$RESET$YELLOW${BOLD}この子は1分間に約${recoveryMana.recoveryMana}マナ",
                s"$RESET$YELLOW${BOLD}回復させる力を持っているようです。"
              )
            )
          ).apply(player)
        }

      }
    }
  }

}
