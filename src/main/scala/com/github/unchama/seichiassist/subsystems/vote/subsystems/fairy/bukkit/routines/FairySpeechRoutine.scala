package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.{ConcurrentEffect, IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitFairySpeak
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

object FairySpeechRoutine {

  def start(player: Player)(
    implicit fairyAPI: FairyAPI[SyncIO],
    concurrentEffect: ConcurrentEffect[SyncIO],
    context: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO]
  ): IO[Nothing] = {

    val repeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minute
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      onMainThread.runAction {
        BukkitFairySpeak[SyncIO].speakRandomly(player)
      }
    )
  }

}
