package com.github.unchama.buildassist.task

import java.math.BigDecimal

import com.github.unchama.buildassist.BuildAssist
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class UpdateBuildCountTask extends BukkitRunnable {
  override def run(): Unit = {
    BuildAssist.playermap.values.filterNot(_.isOffline).foreach({playerdata =>
      val player = Bukkit.getServer.getPlayer(playerdata.uuid)

      // 1分間の建築量を加算する
      val actualBuildCount = playerdata.buildCountBuffer.doubleValue
      val limit = BuildAssist.config.getBuildNum1minLimit
      // デルタが上限以上なら上限に丸める
      val delta = if (actualBuildCount > limit)
        new BigDecimal(limit)
      else
        playerdata.buildCountBuffer
      playerdata.totalBuildCount = playerdata.totalBuildCount.add(delta)
      playerdata.buildCountBuffer = BigDecimal.ZERO
      playerdata.updateLevel(player)
      playerdata.save(player)
    })
  }
}
