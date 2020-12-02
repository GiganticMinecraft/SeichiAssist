package com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.global

import cats.effect.{IO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.seichiassist.subsystems.autosave.application.SystemConfiguration
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.WorldSaveTask
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

import scala.concurrent.duration.FiniteDuration

object WorldSaveRoutine {
  def apply()(implicit configuration: SystemConfiguration, context: RepeatingTaskContext): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minutes
    }

    val routineAction = IO {
      if (configuration.autoSaveEnabled) {
        import scala.jdk.CollectionConverters._

        Util.sendEveryMessage(s"${AQUA}ワールドデータセーブ中…")
        Bukkit.getLogger.info(s"${AQUA}ワールドデータセーブ中…")

        Bukkit.getServer.getWorlds.asScala.foreach(WorldSaveTask.saveWorld)

        Util.sendEveryMessage(s"${AQUA}ワールドデータセーブ完了")
        Bukkit.getLogger.info(s"${AQUA}ワールドデータセーブ完了")
      }
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
