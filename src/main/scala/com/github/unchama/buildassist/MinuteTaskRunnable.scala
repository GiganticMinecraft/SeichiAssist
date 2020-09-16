package com.github.unchama.buildassist

import java.math.BigDecimal

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class MinuteTaskRunnable extends BukkitRunnable {

  override def run(): Unit = {
    BuildAssist.playermap.values.foreach { playerdata: PlayerData =>
      if (!playerdata.isOffline) {
        val player = Bukkit.getServer.getPlayer(playerdata.uuid)

        //1分間の建築量を加算する
        playerdata.totalbuildnum = {
          if (playerdata.build_num_1min.doubleValue > BuildAssist.config.getBuildNum1minLimit) {
            playerdata.totalbuildnum.add(new BigDecimal(BuildAssist.config.getBuildNum1minLimit))
          } else {
            playerdata.totalbuildnum.add(playerdata.build_num_1min)
          }
        }
        playerdata.build_num_1min = BigDecimal.ZERO

        playerdata.updateLevel(player)
        playerdata.buildsave(player)
      }
    }
  }
}