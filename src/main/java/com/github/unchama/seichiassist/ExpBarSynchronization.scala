package com.github.unchama.seichiassist

import org.bukkit.Bukkit
import org.bukkit.boss.{BarColor, BarStyle, BossBar}
import org.bukkit.entity.Player

class ExpBarSynchronization {
  private case class ExpBarProperties(val title: String, val progress: Double)

  private val managedExpBars: MutableMap[Player, BossBar] = HashMap()

  private def computePropertiesFor(player: Player): ExpBarProperties {
    val playerData = SeichiAssist.playermap[player.uniqueId]!!
    val playerLevel = playerData.level

    return if (playerLevel >= LevelThresholds.levelExpThresholds.size) {
      // BarをMAXにして総整地量を表示
      val text = s"$GOLD${BOLD}Lv $playerLevel(総整地量: ${String.format("%,d", playerData.totalbreaknum)})"
      val progress = 1.0

      ExpBarProperties(text, progress)
    } else {
      // 現在のLvにおける割合をBarに配置
      val nextLevelThreshold = LevelThresholds.levelExpThresholds[playerLevel]
      val previousLevelThreshold = LevelThresholds.levelExpThresholds[playerLevel - 1]
      val currentExp = playerData.totalbreaknum
      val text = s"$GOLD${BOLD}Lv $playerLevel(${String.format("%,d", currentExp)}/${String.format("%,d", nextLevelThreshold)})"

      val expAfterPreviousThreshold = currentExp - previousLevelThreshold
      val expBetweenLevels = nextLevelThreshold - previousLevelThreshold
      val progress = when {
        // レベルアップ前にログアウトした場合、次回ログイン時のレベルアップ処理までに100%を超えている場合がある
        expAfterPreviousThreshold >= expBetweenLevels => 1.0
        expAfterPreviousThreshold <= 0 => 0.0
        else => expAfterPreviousThreshold.toDouble() / expBetweenLevels
      }

      ExpBarProperties(text, progress)
    }
  }

  def synchronizeFor(player: Player) {
    desynchronizeFor(player)

    val playerData = SeichiAssist.playermap[player.uniqueId]!!

    if (playerData.settings.isExpBarVisible) {
      val (title, progress) = computePropertiesFor(player)

      managedExpBars[player] = Bukkit.getServer().createBossBar(title, BarColor.YELLOW, BarStyle.SOLID).apply {
        this.progress = progress
        addPlayer(player)
      }
    }
  }

  def desynchronizeFor(player: Player) {
    managedExpBars[player]?.removeAll()
    managedExpBars.remove(player)
  }
}
