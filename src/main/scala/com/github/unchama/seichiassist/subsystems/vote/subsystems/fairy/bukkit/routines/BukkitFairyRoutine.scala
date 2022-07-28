package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.{ConcurrentEffect, IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitRecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyUsingState
import org.bukkit.entity.Player

import java.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration

class BukkitFairyRoutine extends FairyRoutine[IO, SyncIO, Player] {
  override def start(player: Player)(
    implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    fairyAPI: FairyAPI[IO, SyncIO, Player],
    voteAPI: VoteAPI[IO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    context: RepeatingTaskContext
  ): IO[Nothing] = {

    val repeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minute
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    implicit val onMainThread: OnMinecraftServerThread[IO] =
      PluginExecutionContexts.onMainThread

    implicit val ioCE: ConcurrentEffect[IO] =
      IO.ioConcurrentEffect(PluginExecutionContexts.asyncShift)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      onMainThread.runAction {
        if (fairyAPI.fairyUsingState(player).unsafeRunSync() == FairyUsingState.Using) {
          if (
            fairyAPI
              .fairyEndTime(player)
              .unsafeRunSync()
              .get
              .endTimeOpt
              .get
              .isBefore(LocalDateTime.now())
          ) {
            new FairySpeech[IO, SyncIO].bye(player).runAsync(_ => IO.unit)
          } else {
            BukkitRecoveryMana[IO, SyncIO](player).recovery.runAsync(_ => IO.unit)
          }
        } else {
          SyncIO.unit
        }
      }
    )
  }
}
