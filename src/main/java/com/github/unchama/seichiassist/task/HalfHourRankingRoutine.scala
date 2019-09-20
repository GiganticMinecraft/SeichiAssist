package com.github.unchama.seichiassist.task

object HalfHourRankingRoutine: RepeatedTaskLauncher() {
  override def getRepeatIntervalTicks(): Long = if (SeichiAssist.DEBUG) 20 * 20 else 20 * 60 * 30

  override suspend def runRoutine() {
    Util.sendEveryMessage("--------------30分間整地ランキング--------------")

    var totalBreakCount = 0

    // playermapに入っているすべてのプレイヤーデータについて処理
    for (playerData in SeichiAssist.playermap.values) {
      val player = Bukkit.getPlayer(playerData.uuid)
      val halfHourBlock = playerData.halfhourblock

      //プレイヤーがオンラインの時の処理
      if (player != null) {
        val totalBreakNum = playerData.totalbreaknum

        halfHourBlock.after = totalBreakNum
        halfHourBlock.setIncrease()
        halfHourBlock.before = totalBreakNum

        //increaseが0超過の場合プレイヤー個人に個人整地量を通知
        if (halfHourBlock.increase > 0) {
          player.sendMessage(s"あなたの整地量は ${ChatColor.AQUA}${halfHourBlock.increase}${ChatColor.WHITE} でした")
        }
      } else {
        //ﾌﾟﾚｲﾔｰがオフラインの時の処理
        //前回との差を０に設定
        halfHourBlock.increase = 0
      }

      //allに30分間の採掘量を加算
      totalBreakCount += halfHourBlock.increase.toInt()
    }

    // ここで、0 => 第一位、 1 => 第二位、・・・n => 第(n+1)位にする (つまり降順)
    val sortedPlayerData = SeichiAssist.playermap.values.toList()
        .filter { it.halfhourblock.increase != 0L }
        .sortedBy { it.halfhourblock.increase }
        .asReversed()

    Util.sendEveryMessage("全体の整地量は " + ChatColor.AQUA + totalBreakCount + ChatColor.WHITE + " でした")

    val topPlayerData = sortedPlayerData.firstOrNull()

    // 第一位の整地量が非ゼロならば
    if (topPlayerData != null) {
      val rankingPositionColor = listOf(ChatColor.DARK_PURPLE, ChatColor.BLUE, ChatColor.DARK_AQUA)

      sortedPlayerData
          .take(3) // 1から3位まで
          .zip(rankingPositionColor)
          .forEachIndexed { index, (playerData, positionColor) =>
            val playerNameText = s"$positionColor[ Lv${playerData.level} ]${playerData.lowercaseName}${ChatColor.WHITE}"
            val increaseAmountText = s"${ChatColor.AQUA}${playerData.halfhourblock.increase}${ChatColor.WHITE}"

            Util.sendEveryMessage(s"整地量第${index + 1}位は${playerNameText}で${increaseAmountText}でした")
          }
    }

    Util.sendEveryMessage("--------------------------------------------------")
  }
}
