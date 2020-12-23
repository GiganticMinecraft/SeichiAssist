package com.github.unchama.buildassist

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class MinuteTaskRunnable extends BukkitRunnable {

  override def run(): Unit = {
    BuildAssist.playermap.values.foreach { playerdata: PlayerData =>
      if (!playerdata.isOffline) {
        val player = Bukkit.getServer.getPlayer(playerdata.uuid)

        playerdata.flush1MinuteBuildCount()
        playerdata.notifyPlayerAndUpdateLevel(player)
        playerdata.normalizeAndWriteDataToSeichiAssistPlayerData()
      }
    }
  }
}