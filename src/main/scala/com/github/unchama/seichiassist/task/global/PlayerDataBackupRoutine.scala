package com.github.unchama.seichiassist.task.global

import cats.effect.{IO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.task.PlayerDataSaveTask
import com.github.unchama.seichiassist.util.SendMessageEffect
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

import scala.concurrent.duration.FiniteDuration

object PlayerDataBackupRoutine {
  def apply()(implicit context: RepeatingTaskContext): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      10.minutes
    }

    import cats.implicits._

    val routineAction: IO[Boolean] = {
      val save = {
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread

        import scala.jdk.CollectionConverters._

        for {
          _ <- IO {
            SendMessageEffect.sendMessageToEveryoneIgnoringPreference(s"${AQUA}プレイヤーデータセーブ中…")
            Bukkit.getLogger.info(s"${AQUA}プレイヤーデータセーブ中…")
          }
          players <- IO {
            Bukkit.getOnlinePlayers.asScala.toList
          }
          _ <- players.traverse { player =>
            PlayerDataSaveTask
              .savePlayerData[IO](player, SeichiAssist.playermap(player.getUniqueId))
          }
          _ <- IO {
            SendMessageEffect.sendMessageToEveryoneIgnoringPreference(s"${AQUA}プレイヤーデータセーブ完了")
            Bukkit.getLogger.info(s"${AQUA}プレイヤーデータセーブ完了")
          }
        } yield ()
      }

      for {
        saveRequired <- IO {
          SeichiAssist.playermap.nonEmpty
        }
        _ <- if (saveRequired) save else IO.unit
      } yield true
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
