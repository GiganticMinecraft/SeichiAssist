package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
object PlayerDataBackupTask extends RepeatedTaskLauncher() {
  override def getRepeatIntervalTicks(): Long = if (SeichiAssist.DEBUG) 20 * 20 else 20 * 60 * 10

  override @SuspendingMethod def runRoutine() {
    val playerMap = SeichiAssist.playermap
    val databaseGateway = SeichiAssist.databaseGateway

    if (playerMap.isEmpty()) return

    Util.sendEveryMessage(AQUA.toString() + "プレイヤーデータセーブ中…")
    Bukkit.getLogger().info(AQUA.toString() + "プレイヤーデータセーブ中…")

    GlobalScope.launch {
      //現在オンラインのプレイヤーのプレイヤーデータを永続化する
      Bukkit.getOnlinePlayers()
          .map { playerMap[it.uniqueId] }
          .forEach { savePlayerData(it) }
    }

    Util.sendEveryMessage(AQUA.toString() + "プレイヤーデータセーブ完了")
    Bukkit.getLogger().info(AQUA.toString() + "プレイヤーデータセーブ完了")

    //ランキングリストを最新情報に更新する
    if (!databaseGateway.playerDataManipulator.successRankingUpdate()) {
      SeichiAssist.instance.logger.info("ランキングデータの作成に失敗しました")
    }
  }
}
