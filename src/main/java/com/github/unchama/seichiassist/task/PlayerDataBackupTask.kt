package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor

object PlayerDataBackupTask: RepeatedTaskLauncher() {
  override fun getRepeatIntervalTicks(): Long = if (SeichiAssist.DEBUG) 20 * 20 else 20 * 60 * 10

  override suspend fun runRoutine() {
    val playerMap = SeichiAssist.playermap
    val databaseGateway = SeichiAssist.databaseGateway

    if (playerMap.isEmpty()) return

    Util.sendEveryMessage(ChatColor.AQUA.toString() + "プレイヤーデータセーブ中…")
    Bukkit.getLogger().info(ChatColor.AQUA.toString() + "プレイヤーデータセーブ中…")

    //現在オンラインのプレイヤーのプレイヤーデータを永続化する
    for (player in Bukkit.getOnlinePlayers()) {
      val playerData = playerMap[player.uniqueId]

      if (playerData !== null) {
        databaseGateway.playerDataManipulator.savePlayerData(playerData)
      } else {
        Bukkit.getLogger().warning(player.name + " -> PlayerData not found.")
        Bukkit.getLogger().warning("PlayerDataBackupTask")
      }
    }

    Util.sendEveryMessage(ChatColor.AQUA.toString() + "プレイヤーデータセーブ完了")
    Bukkit.getLogger().info(ChatColor.AQUA.toString() + "プレイヤーデータセーブ完了")

    //ランキングリストを最新情報に更新する
    if (!databaseGateway.playerDataManipulator.successRankingUpdate()) {
      SeichiAssist.instance.logger.info("ランキングデータの作成に失敗しました")
    }
  }
}
