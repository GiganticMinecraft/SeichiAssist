package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions

import cats.effect.{ConcurrentEffect, ContextShift, IO, LiftIO, SyncIO}
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.domain.EffectPoint
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines.FairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpawnRequest
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairySpawnRequestResult._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyRecoveryManaAmount,
  FairyUsingState
}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

import scala.concurrent.ExecutionContext

object BukkitSummonFairy {

  def apply(player: Player)(
    implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    fairyAPI: FairyAPI[IO, Player],
    voteAPI: VoteAPI[IO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    concurrentEffect: ConcurrentEffect[IO]
  ): SummonFairy[IO] = new SummonFairy[IO] {
    override def summon: IO[Unit] = {
      val failedEffect = spawnFailedEffect(player, _)

      new FairySpawnRequest[IO, SyncIO, Player].spawnRequest(player).unsafeRunSync() match {
        case NotEnoughSeichiLevel =>
          failedEffect(s"${GOLD}プレイヤーレベルが足りません")
        case AlreadyFairySpawned =>
          failedEffect(s"${GOLD}既に妖精を召喚しています")
        case NotEnoughEffectPoint =>
          failedEffect(s"${GOLD}投票ptが足りません")
        case Success =>
          val levelCappedManaAmount =
            manaApi.readManaAmount(player).unsafeRunSync().cap.value

          // 回復するマナの量
          val recoveryMana = FairyRecoveryManaAmount.manaAmountAt(levelCappedManaAmount)

          val eff = for {
            fairySummonCost <- fairyAPI.fairySummonCost(player)
            _ <- fairyAPI.updateFairyUsingState(player, FairyUsingState.Using)
            uuid = player.getUniqueId
            _ <- voteAPI.decreaseEffectPoint(uuid, EffectPoint(fairySummonCost.value * 2))
            _ <- fairyAPI.updateFairyRecoveryManaAmount(uuid, recoveryMana)
            isFairyEndTimeDefined <- fairyAPI.fairyEndTime(player)
            _ <- fairyAPI.updateFairyEndTime(player, fairySummonCost.validTime)
          } yield {
            /*
              FairySpeechRoutineが一度も起動されていなければ起動する
             */
            if (isFairyEndTimeDefined.isEmpty) {
              import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sleepAndRoutineContext
              implicit val contextShift: ContextShift[IO] =
                IO.contextShift(ExecutionContext.global)
              FairyRoutine.start(player).start.unsafeRunSync()
            }
          }

          LiftIO[IO].liftIO {
            SequentialEffect(
              UnfocusedEffect {
                eff.unsafeRunSync()
                new FairySpeech().summonSpeech(player).unsafeRunSync()
              },
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

  private def spawnFailedEffect(player: Player, message: String): IO[Unit] = {
    SequentialEffect(
      MessageEffect(message),
      FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
    ).run(player)
  }

}
