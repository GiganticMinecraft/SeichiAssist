package com.github.unchama.buildassist

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class MinuteTaskRunnable extends BukkitRunnable {

  override def run(): Unit = {
    Bukkit.getOnlinePlayers.forEach { player =>
      val playerData = BuildAssist.playermap(player.getUniqueId)

      playerData.flush1MinuteBuildCount()
      playerData.notifyPlayerAndUpdateLevel(player)
      playerData.normalizeAndWriteDataToSeichiAssistPlayerData()
    }
  }
}