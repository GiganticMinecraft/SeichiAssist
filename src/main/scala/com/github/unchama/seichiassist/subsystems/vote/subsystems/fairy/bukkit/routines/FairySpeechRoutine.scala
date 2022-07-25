package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.{ConcurrentEffect, Sync, SyncEffect, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitFairySpeak
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

object FairySpeechRoutine {

  def start[F[_]: ConcurrentEffect: Timer, G[_]: SyncEffect](player: Player)(
    implicit fairyAPI: FairyAPI[F],
    context: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[F]
  ): F[Nothing] = {

    val repeatInterval: F[FiniteDuration] = Sync[F].pure {
      import scala.concurrent.duration._

      1.minute
    }

    RepeatingRoutine.permanentRoutine[F, G](
      repeatInterval,
      onMainThread.runAction {
        BukkitFairySpeak[F].speakRandomly(player)
      }
    )
  }

}
