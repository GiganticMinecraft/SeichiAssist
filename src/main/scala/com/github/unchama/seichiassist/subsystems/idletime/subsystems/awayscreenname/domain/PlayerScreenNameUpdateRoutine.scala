package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain

import cats.effect.{IO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread

trait PlayerScreenNameUpdateRoutine[Player] {

  import scala.concurrent.duration._

  def start(player: Player)(
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    updatePlayerScreenName: UpdatePlayerScreenName[IO, Player]
  ): IO[Nothing] = {
    val repeatInterval: IO[FiniteDuration] = IO(1.minute)

    implicit val timer: Timer[IO] = IO.timer(repeatingTaskContext)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      onMainThread.runAction {
        updatePlayerScreenName.updatePlayerNameColor(player).runAsync(_ => IO.unit)
      }
    )
  }

}
