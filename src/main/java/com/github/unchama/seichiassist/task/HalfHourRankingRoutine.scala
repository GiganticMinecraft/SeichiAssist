package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
object HalfHourRankingRoutine extends RepeatedTaskLauncher() {
  override def getRepeatIntervalTicks(): Long = if (SeichiAssist.DEBUG) 20 * 20 else 20 * 60 * 30

  override @SuspendingMethod def runRoutine() {
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
          player.sendMessage(s"あなたの整地量は ${AQUA}${halfHourBlock.increase}${WHITE} でした")
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

    Util.sendEveryMessage("全体の整地量は " + AQUA + totalBreakCount + WHITE + " でした")

    val topPlayerData = sortedPlayerData.firstOrNull()

    // 第一位の整地量が非ゼロならば
    if (topPlayerData != null) {
      val rankingPositionColor = List(DARK_PURPLE, BLUE, DARK_AQUA)

      sortedPlayerData
          .take(3) // 1から3位まで
          .zip(rankingPositionColor)
          .forEachIndexed { index, (playerData, positionColor) =>
            val playerNameText = s"$positionColor[ Lv${playerData.level} ]${playerData.lowercaseName}${WHITE}"
            val increaseAmountText = s"${AQUA}${playerData.halfhourblock.increase}${WHITE}"

            Util.sendEveryMessage(s"整地量第${index + 1}位は${playerNameText}で${increaseAmountText}でした")
          }
    }

    Util.sendEveryMessage("--------------------------------------------------")
  }
}
