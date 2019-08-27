package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.LevelThresholds
import com.github.unchama.seichiassist.data.player.PlayerData
import org.bukkit.Bukkit
import org.bukkit.ChatColor.BOLD
import org.bukkit.ChatColor.GOLD
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player


@Deprecated("isVisibleはPlayerData.configurationで管理されるべきであり、ExpBarを中央管理して各PlayerDataと同期するようにするべき")
class ExpBar(private val playerData: PlayerData, private val p: Player) {
  private var expBar: BossBar = p.server.createBossBar("", BarColor.YELLOW, BarStyle.SOLID).also { isVisible = false }

  var isVisible: Boolean
    get() = expBar.isVisible
    set(visible) {
      expBar.isVisible = visible
      calculate()
    }

  fun remove() = expBar.removeAll()

  fun calculate() {
    if (!expBar.isVisible) return

    remove()

    // レベル上限の人
    val (barText, barProgress) = if (playerData.level >= LevelThresholds.levelExpThresholds.size) {
      // BarをMAXにして総整地量を表示
      val text = "$GOLD${BOLD}Lv ${playerData.level}(総整地量: ${String.format("%,d", playerData.totalbreaknum)})"
      val progress = 1.0

      text to progress
    } else {
      // 現在のLvにおける割合をBarに配置
      val nextLevelThreshold = LevelThresholds.levelExpThresholds[playerData.level]
      val previousLevelThreshold = LevelThresholds.levelExpThresholds[playerData.level - 1]
      val currentExp = playerData.totalbreaknum
      val text = "$GOLD${BOLD}Lv ${playerData.level}(${String.format("%,d", currentExp)}/${String.format("%,d", nextLevelThreshold)})"

      val expAfterPreviousThreshold = playerData.totalbreaknum - previousLevelThreshold
      val expBetweenLevels = nextLevelThreshold - previousLevelThreshold
      val progress = when {
        // レベルアップ前にログアウトした場合、次回ログイン時のレベルアップ処理までに100%を超えている場合がある
        expAfterPreviousThreshold >= expBetweenLevels -> 1.0
        expAfterPreviousThreshold <= 0 -> 0.0
        else -> expAfterPreviousThreshold.toDouble() / expBetweenLevels
      }

      text to progress
    }

    expBar = Bukkit.getServer().createBossBar(barText, BarColor.YELLOW, BarStyle.SOLID).apply {
      progress = barProgress
      addPlayer(p)
    }

    // 描画処理
    playerData.activeskilldata.mana.display(p, playerData.level)
  }
}
