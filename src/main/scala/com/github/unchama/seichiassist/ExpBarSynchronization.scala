package com.github.unchama.seichiassist

import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.boss.{BarColor, BarStyle, BossBar}
import org.bukkit.entity.Player

import scala.collection.mutable

class ExpBarSynchronization {

  import com.github.unchama.util.syntax._

  private val managedExpBars: mutable.HashMap[Player, BossBar] = mutable.HashMap()

  def synchronizeFor(player: Player): Unit = {
    desynchronizeFor(player)

    val playerData = SeichiAssist.playermap(player.getUniqueId)

    if (playerData.settings.isExpBarVisible) {
      val ExpBarProperties(title, progress) = computePropertiesFor(player)

      managedExpBars(player) =
        Bukkit.getServer.createBossBar(title, BarColor.YELLOW, BarStyle.SOLID)
          .modify { b =>
            import b._
            setProgress(progress)
            addPlayer(player)
          }
    }
  }

  private def computePropertiesFor(player: Player): ExpBarProperties = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    val playerLevel = playerData.level

    if (playerLevel >= LevelThresholds.levelExpThresholds.size) {
      // BarをMAXにして総整地量を表示
      val stars = playerData.starLevels.fromBreakAmount
      val starText = if (stars > 0) s"☆$stars" else ""
      val text = s"$GOLD${BOLD}Lv $playerLevel$starText (総整地量: ${String.format("%,d", playerData.totalbreaknum)})"
      val progress = 1.0

      ExpBarProperties(text, progress)
    } else {
      // 現在のLvにおける割合をBarに配置
      val nextLevelThreshold = LevelThresholds.levelExpThresholds(playerLevel)
      val previousLevelThreshold = LevelThresholds.levelExpThresholds(playerLevel - 1)
      val currentExp = playerData.totalbreaknum
      val text = s"$GOLD${BOLD}Lv $playerLevel(${String.format("%,d", currentExp)}/${String.format("%,d", nextLevelThreshold)})"

      val expAfterPreviousThreshold = currentExp - previousLevelThreshold
      val expBetweenLevels = nextLevelThreshold - previousLevelThreshold
      val progress = {
        // レベルアップ前にログアウトした場合、次回ログイン時のレベルアップ処理までに100%を超えている場合がある
        if (expAfterPreviousThreshold >= expBetweenLevels) 1.0
        else if (expAfterPreviousThreshold <= 0) 0.0
        else expAfterPreviousThreshold.toDouble / expBetweenLevels
      }

      ExpBarProperties(text, progress)
    }
  }

  def desynchronizeFor(player: Player): Unit = {
    managedExpBars.get(player).foreach(_.removeAll())
    managedExpBars.remove(player)
  }

  private case class ExpBarProperties(title: String, progress: Double)
}
