package com.github.unchama.seichiassist.task

object PlayerDataBackupTask extends RepeatedTaskLauncher() {
  override def getRepeatIntervalTicks(): Long = if (SeichiAssist.DEBUG) 20 * 20 else 20 * 60 * 10

  override suspend def runRoutine() {
    val playerMap = SeichiAssist.playermap
    val databaseGateway = SeichiAssist.databaseGateway

    if (playerMap.isEmpty()) return

    Util.sendEveryMessage(ChatColor.AQUA.toString() + "プレイヤーデータセーブ中…")
    Bukkit.getLogger().info(ChatColor.AQUA.toString() + "プレイヤーデータセーブ中…")

    GlobalScope.launch {
      //現在オンラインのプレイヤーのプレイヤーデータを永続化する
      Bukkit.getOnlinePlayers()
          .map { playerMap[it.uniqueId]!! }
          .forEach { savePlayerData(it) }
    }

    Util.sendEveryMessage(ChatColor.AQUA.toString() + "プレイヤーデータセーブ完了")
    Bukkit.getLogger().info(ChatColor.AQUA.toString() + "プレイヤーデータセーブ完了")

    //ランキングリストを最新情報に更新する
    if (!databaseGateway.playerDataManipulator.successRankingUpdate()) {
      SeichiAssist.instance.logger.info("ランキングデータの作成に失敗しました")
    }
  }
}
