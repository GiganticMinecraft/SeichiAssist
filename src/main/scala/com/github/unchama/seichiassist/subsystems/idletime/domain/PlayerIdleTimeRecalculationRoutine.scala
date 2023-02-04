package com.github.unchama.seichiassist.subsystems.idletime.domain

import cats.effect.{IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread

trait PlayerIdleTimeRecalculationRoutine[Player] {

  import scala.concurrent.duration._

  /**
   * @return リポジトリのデータを現在のプレイヤーの位置と放置時間に更新する作用
   */
  def updatePlayerLocationAndPlayerIdleMinute: SyncIO[Unit]

  final def start(
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO]
  ): IO[Nothing] = {
    val repeatInterval: IO[FiniteDuration] = IO(1.minute)

    implicit val timer: Timer[IO] = IO.timer(repeatingTaskContext)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      onMainThread.runAction {
        updatePlayerLocationAndPlayerIdleMinute
      }
    )
  }

}
