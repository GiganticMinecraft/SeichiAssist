package com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.global

import cats.effect.{IO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.Config
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.WorldSaveTask
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

import scala.concurrent.duration.FiniteDuration

object WorldSaveRoutine {
  def apply()(implicit context: RepeatingTaskContext): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      if (SeichiAssist.DEBUG) 20.seconds else 10.minutes
    }

    val routineAction: IO[Boolean] = {
      val save = IO {
        if (Config.loadFrom(SeichiAssist.instance).isAutoSaveEnabled) {
          import scala.jdk.CollectionConverters._

          Util.sendEveryMessage(s"${AQUA}ワールドデータセーブ中…")
          Bukkit.getLogger.info(s"${AQUA}ワールドデータセーブ中…")

          Bukkit.getServer.getScheduler.runTask(
            SeichiAssist.instance,
            () => Bukkit.getServer.getWorlds.asScala.foreach(WorldSaveTask.saveWorld)
          )

          Util.sendEveryMessage(s"${AQUA}ワールドデータセーブ完了")
          Bukkit.getLogger.info(s"${AQUA}ワールドデータセーブ完了")
        }
      }

      for {
        _ <- save
      } yield true
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}