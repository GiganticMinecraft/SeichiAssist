package com.github.unchama.seichiassist.task

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

import scala.concurrent.ExecutionContext
object PlayerDataBackupTask extends RepeatedTaskLauncher() {
  override val getRepeatIntervalTicks: IO[Long] = IO {
    if (SeichiAssist.DEBUG) 20 * 20 else 20 * 60 * 10
  }

  override val runRoutine: IO[Unit] = {
    val save = IO {
      import scala.jdk.CollectionConverters._

      if (SeichiAssist.playermap.nonEmpty) {
        Util.sendEveryMessage(s"${AQUA}プレイヤーデータセーブ中…")
        Bukkit.getLogger().info(s"${AQUA}プレイヤーデータセーブ中…")

        //現在オンラインのプレイヤーのプレイヤーデータを永続化する
        Bukkit.getOnlinePlayers.asScala.toList
          .map(player => SeichiAssist.playermap(player.getUniqueId))
          .foreach(PlayerDataSaving.savePlayerData)

        Util.sendEveryMessage(s"${AQUA}プレイヤーデータセーブ完了")
        Bukkit.getLogger().info(s"${AQUA}プレイヤーデータセーブ完了")
      }
    }

    val updateRankingData = IO {
      //ランキングリストを最新情報に更新する
      if (!SeichiAssist.databaseGateway.playerDataManipulator.successRankingUpdate()) {
        SeichiAssist.instance.getLogger.info("ランキングデータの作成に失敗しました")
      }
    }

    for {
      _ <- IO.shift(ExecutionContext.global)
      _ <- save
      _ <- updateRankingData
    } yield ()
  }
}
