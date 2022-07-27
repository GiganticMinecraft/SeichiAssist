package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway

import cats.effect.{ConcurrentEffect, ContextShift, IO, LiftIO, SyncIO}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.domain.EffectPoint
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitFairySpeak
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines.FairySpeechRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpawnGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyRecoveryManaAmount,
  FairyUsingState
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor.{BOLD, GOLD, RESET, YELLOW}
import org.bukkit.Sound
import org.bukkit.entity.Player

import scala.concurrent.ExecutionContext

class BukkitFairySpawnGateway(player: Player)(
  implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
  fairyAPI: FairyAPI[IO, Player],
  voteAPI: VoteAPI[IO],
  manaApi: ManaApi[IO, SyncIO, Player],
  serviceRepository: PlayerDataRepository[FairySpeechService[SyncIO]],
  concurrentEffect: ConcurrentEffect[IO]
) extends FairySpawnGateway[SyncIO] {

  /**
   * 妖精をスポーンさせる作用
   */
  override def spawn: SyncIO[Unit] = {
    val playerLevel =
      breakCountAPI
        .seichiAmountDataRepository(player)
        .read
        .unsafeRunSync()
        .levelCorrespondingToExp
        .level

    val uuid = player.getUniqueId

    val fairySummonCost = fairyAPI.fairySummonCost(player).unsafeRunSync()

    val failedEffect = spawnFailedEffect(player, _)

    if (playerLevel < 10)
      return failedEffect(s"${GOLD}プレイヤーレベルが足りません")

    if (fairyAPI.fairyUsingState(uuid).unsafeRunSync() == FairyUsingState.Using)
      return failedEffect(s"${GOLD}既に妖精を召喚しています")

    if (voteAPI.effectPoints(uuid).unsafeRunSync().value < fairySummonCost.value * 2)
      return failedEffect(s"${GOLD}投票ptが足りません")

    val levelCappedManaAmount =
      manaApi.readManaAmount(player).unsafeRunSync().cap.value

    // 回復するマナの量
    val recoveryMana = FairyRecoveryManaAmount.manaAmountAt(levelCappedManaAmount)

    val eff = for {
      _ <- fairyAPI.updateFairyUsingState(uuid, FairyUsingState.Using)
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
        FairySpeechRoutine.start(player).start.unsafeRunSync()
      }
    }

    LiftIO[IO]
      .liftIO {
        SequentialEffect(
          UnfocusedEffect {
            eff.unsafeRunSync()
            new FairySpeech().summonSpeech(player)
            BukkitFairySpeak[IO].speakStartMessage(player).unsafeRunSync()
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
      .runAsync(_ => IO.unit)
  }

  private def spawnFailedEffect(message: String): SyncIO[Unit] = {
    SequentialEffect(
      MessageEffect(message),
      FocusedSoundEffect(Sound.BLOCK_GLASS_PLACE, 1f, 0.1f)
    ).run(player).runAsync(_ => IO.unit)
  }

  /**
   * 妖精をデスポーンさせる作用
   */
  override def despawn(): SyncIO[Unit] = ???
}
