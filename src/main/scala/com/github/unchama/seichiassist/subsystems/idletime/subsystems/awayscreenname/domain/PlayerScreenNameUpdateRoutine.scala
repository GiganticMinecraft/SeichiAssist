package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain

import cats.effect.{IO, SyncIO}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.ioRuntime

trait PlayerScreenNameUpdateRoutine[Player] {

  import scala.concurrent.duration._

  def start(player: Player)(
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    updatePlayerScreenName: UpdatePlayerScreenName[IO, Player]
  ): IO[Nothing] = {
    val repeatInterval: IO[FiniteDuration] = IO(1.minute)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      onMainThread.runAction {
        SyncIO(updatePlayerScreenName.updatePlayerNameColor(player).unsafeRunAndForget())
      }
    )
  }

}
