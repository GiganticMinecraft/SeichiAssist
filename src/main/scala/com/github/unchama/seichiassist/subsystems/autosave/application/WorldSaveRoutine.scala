package com.github.unchama.seichiassist.subsystems.autosave.application

import cats.Applicative
import cats.effect.{Sync, Timer}
import com.github.unchama.concurrent.RepeatingRoutine
import org.bukkit.ChatColor.AQUA

object WorldSaveRoutine {
  def apply[F[_]: Sync: CanSaveWorlds: CanNotifySaves: Timer]()(
    implicit configuration: SystemConfiguration
  ): F[Nothing] = {
    import cats.implicits._

    import scala.concurrent.duration._

    val getRepeatInterval: F[FiniteDuration] = Applicative[F].pure(10.minutes)

    val routineAction: F[Unit] =
      if (configuration.autoSaveEnabled)
        CanNotifySaves[F].notify(s"${AQUA}ワールドデータセーブ中…") >>
          CanSaveWorlds[F].saveAllWorlds >>
          CanNotifySaves[F].notify(s"${AQUA}ワールドデータセーブ完了")
      else
        Applicative[F].unit

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
