package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable

class PlayerDataBackupTask : BukkitRunnable() {
  private val plugin = SeichiAssist.instance
  private val databaseGateway = SeichiAssist.databaseGateway
  private val playermap = SeichiAssist.playermap

  override fun run() {
    //playermapが空の時return
    if (playermap.isEmpty()) return

    Util.sendEveryMessage(ChatColor.AQUA.toString() + "プレイヤーデータセーブ中…")
    Bukkit.getLogger().info(ChatColor.AQUA.toString() + "プレイヤーデータセーブ中…")

    //現在オンラインのプレイヤーのプレイヤーデータを永続化する
    for (player in Bukkit.getOnlinePlayers()) {
      val playerData = playermap[player.uniqueId]

      if (playerData != null) {
        databaseGateway.playerDataManipulator.savePlayerData(playerData)
      } else {
        Bukkit.getLogger().warning(player.name + " -> PlayerData not found.")
        Bukkit.getLogger().warning("PlayerDataBackupTask")
      }
    }

    Util.sendEveryMessage(ChatColor.AQUA.toString() + "プレイヤーデータセーブ完了")
    Bukkit.getLogger().info(ChatColor.AQUA.toString() + "プレイヤーデータセーブ完了")

    //ランキングリストを最新情報に更新する
    if (!databaseGateway.playerDataManipulator.updateAllRankingList()) {
      plugin.logger.info("ランキングデータの作成に失敗しました")
    }
  }

}
