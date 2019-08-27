package com.github.unchama.seichiassist

import org.bukkit.Bukkit
import org.bukkit.ChatColor.BOLD
import org.bukkit.ChatColor.GOLD
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

class ExpBarSynchronization {
  private data class ExpBarProperties(val title: String, val progress: Double)

  private val managedExpBars: MutableMap<Player, BossBar> = HashMap()

  private fun computePropertiesFor(player: Player): ExpBarProperties {
    val playerData = SeichiAssist.playermap[player.uniqueId]!!
    val playerLevel = playerData.level

    return if (playerLevel >= LevelThresholds.levelExpThresholds.size) {
      // BarをMAXにして総整地量を表示
      val text = "$GOLD${BOLD}Lv $playerLevel(総整地量: ${String.format("%,d", playerData.totalbreaknum)})"
      val progress = 1.0

      ExpBarProperties(text, progress)
    } else {
      // 現在のLvにおける割合をBarに配置
      val nextLevelThreshold = LevelThresholds.levelExpThresholds[playerLevel]
      val previousLevelThreshold = LevelThresholds.levelExpThresholds[playerLevel - 1]
      val currentExp = playerData.totalbreaknum
      val text = "$GOLD${BOLD}Lv $playerLevel(${String.format("%,d", currentExp)}/${String.format("%,d", nextLevelThreshold)})"

      val expAfterPreviousThreshold = currentExp - previousLevelThreshold
      val expBetweenLevels = nextLevelThreshold - previousLevelThreshold
      val progress = when {
        // レベルアップ前にログアウトした場合、次回ログイン時のレベルアップ処理までに100%を超えている場合がある
        expAfterPreviousThreshold >= expBetweenLevels -> 1.0
        expAfterPreviousThreshold <= 0 -> 0.0
        else -> expAfterPreviousThreshold.toDouble() / expBetweenLevels
      }

      ExpBarProperties(text, progress)
    }
  }

  fun synchronizeFor(player: Player) {
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

  fun desynchronizeFor(player: Player) {
    managedExpBars[player]?.removeAll()
    managedExpBars.remove(player)
  }
}
