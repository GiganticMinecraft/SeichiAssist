package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.{ConcurrentEffect, IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitFairySpeak
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

object FairySpeechRoutine {

  def start(
    player: Player
  )(implicit fairyAPI: FairyAPI[IO, Player], context: RepeatingTaskContext): IO[Nothing] = {

    val repeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minute
    }

    implicit val timer: Timer[IO] = IO.timer(context)
    implicit val ioCE: ConcurrentEffect[IO] =
      IO.ioConcurrentEffect(PluginExecutionContexts.asyncShift)

    implicit val onMainThread: OnMinecraftServerThread[IO] =
      PluginExecutionContexts.onMainThread
    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      onMainThread.runAction {
        SyncIO {
          BukkitFairySpeak[IO].speakRandomly(player).unsafeRunSync()
        }
      }
    )
  }

}
