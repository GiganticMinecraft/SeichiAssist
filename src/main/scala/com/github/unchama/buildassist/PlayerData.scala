package com.github.unchama.buildassist

import com.github.unchama.buildassist.domain.explevel.{BuildAssistExpTable, BuildExpAmount}
import com.github.unchama.seichiassist.{PackagePrivate, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

final class PlayerData(val player: Player) {

  var level = 1
  /**
   * トータル設置ブロック数
   */
  var totalbuildnum: BigDecimal = BigDecimal(0)

  /**
   * 1分のブロック設置数
   */
  @PackagePrivate private[buildassist] var build_num_1min = BigDecimal(0)

  private[buildassist] def notifyPlayerAndUpdateLevel(player: Player): Unit = {
    val oldLevel = level
    val newLevel = BuildAssistExpTable.levelAt(BuildExpAmount.ofNonNegative(totalbuildnum))

    for {
      transitionTarget <- (oldLevel + 1) to newLevel.level
      message = {
        if (transitionTarget != BuildAssistExpTable.maxLevel.level)
          s"${GOLD}ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(${transitionTarget - 1})→建築Lv($transitionTarget)】"
        else
          s"${GOLD}最大Lvに到達したよ(`･ω･´)"
      }
    } {
      player.sendMessage(message)
    }

    level = newLevel.level
  }

  private[buildassist] def loadFrom(player: Player): Unit = {
    val playerdata_s = SeichiAssist.playermap(player.getUniqueId)

    totalbuildnum = playerdata_s.buildCount.count
    level = playerdata_s.buildCount.lv
    notifyPlayerAndUpdateLevel(player)
  }

  def flush1MinuteBuildCount(): Unit = {
    totalbuildnum = totalbuildnum + (build_num_1min max BuildAssist.config.getBuildNum1minLimit)
    build_num_1min = BigDecimal(0)
  }

  def normalizeAndWriteDataToSeichiAssistPlayerData(): Unit = {
    flush1MinuteBuildCount()

    val playerData = SeichiAssist.playermap(player.getUniqueId)
    playerData.buildCount = playerData.buildCount.copy(
      lv = level,
      count = totalbuildnum
    )
  }
}
