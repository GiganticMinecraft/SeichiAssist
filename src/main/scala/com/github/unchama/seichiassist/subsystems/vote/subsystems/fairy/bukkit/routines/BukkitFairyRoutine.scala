package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.{ConcurrentEffect, IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitRecoveryMana
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{FairyPersistence, FairySpeech}
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

class BukkitFairyRoutine(fairySpeech: FairySpeech[IO, Player])(
  implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
  voteAPI: VoteAPI[IO, Player],
  manaApi: ManaApi[IO, SyncIO, Player],
  context: RepeatingTaskContext,
  fairyPersistence: FairyPersistence[IO]
) extends FairyRoutine[IO, SyncIO, Player] {

  override def start(player: Player): IO[Nothing] = {

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
        new BukkitRecoveryMana[IO, SyncIO](player).recovery.runAsync(_ => IO.unit)
      }
    )
  }
}
