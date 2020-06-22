package com.github.unchama.seichiassist.task.global

import cats.effect.IO
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.task.PlayerDataSaveTask
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

import scala.concurrent.duration.FiniteDuration

object PlayerDataBackupRoutine {
  def apply()(implicit context: RepeatingTaskContext): IO[Nothing] = {
    val getRepeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      if (SeichiAssist.DEBUG) 20.seconds else 10.minutes
    }

    val routineAction: IO[Boolean] = {
      val save = IO {
        import scala.jdk.CollectionConverters._

        if (SeichiAssist.playermap.nonEmpty) {
          Util.sendEveryMessage(s"${AQUA}プレイヤーデータセーブ中…")
          Bukkit.getLogger.info(s"${AQUA}プレイヤーデータセーブ中…")

          //現在オンラインのプレイヤーのプレイヤーデータを永続化する
          Bukkit.getOnlinePlayers.asScala.toList
            .map(player => SeichiAssist.playermap(player.getUniqueId))
            .foreach(PlayerDataSaveTask.savePlayerData)

          Util.sendEveryMessage(s"${AQUA}プレイヤーデータセーブ完了")
          Bukkit.getLogger.info(s"${AQUA}プレイヤーデータセーブ完了")
        }
      }

      val updateRankingData = IO {
        //ランキングリストを最新情報に更新する
        if (!SeichiAssist.databaseGateway.playerDataManipulator.successRankingUpdate()) {
          SeichiAssist.instance.getLogger.info("ランキングデータの作成に失敗しました")
        }
      }

      for {
        _ <- save
        _ <- updateRankingData
      } yield true
    }

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}