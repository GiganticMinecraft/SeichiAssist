package com.github.unchama.buildassist

import java.math.BigDecimal

import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.{Bukkit, ChatColor}
import org.bukkit.scheduler.BukkitRunnable

class MinuteTaskRunnable extends BukkitRunnable {

  override def run(): Unit = {
    BuildAssist.playermap.values.foreach { playerdata: PlayerData =>
      if (!playerdata.isOffline) {
        val player = Bukkit.getServer.getPlayer(playerdata.uuid)

        //SeichiAssistのデータを取得
        val uuid = player.getUniqueId
        val playerdata_s = SeichiAssist.playermap(uuid)

        //経験値変更用のクラスを設定
        val expman = new ExperienceManager(player)
        val minus = -BuildAssist.config.getFlyExp

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

        if (playerdata.endlessfly) {
          if (playerdata_s.idleMinute >= 10) {
            player.setAllowFlight(true)
            player.sendMessage(ChatColor.GRAY + "放置時間中のflyは無期限で継続中です(経験値は消費しません)")
          } else if (!expman.hasExp(BuildAssist.config.getFlyExp)) {
            player.sendMessage(ChatColor.RED + "fly効果の発動に必要な経験値が不足しているため、")
            player.sendMessage(ChatColor.RED + "fly効果を終了しました")
            playerdata.flytime = 0
            playerdata.flyflag = false
            playerdata.endlessfly = false
            player.setAllowFlight(false)
            player.setFlying(false)
          } else {
            player.setAllowFlight(true)
            player.sendMessage(ChatColor.GREEN + "fly効果は無期限で継続中です")
            expman.changeExp(minus)
          }
        } else if (playerdata.flyflag) {
          val flytime = playerdata.flytime

          if (playerdata_s.idleMinute >= 10) {
            player.setAllowFlight(true)
            player.sendMessage(ChatColor.GRAY + "放置時間中のflyは無期限で継続中です(経験値は消費しません)")
          } else if (flytime <= 0) {
            player.sendMessage(ChatColor.GREEN + "fly効果が終了しました")
            playerdata.flyflag = false
            player.setAllowFlight(false)
            player.setFlying(false)
          } else if (!expman.hasExp(BuildAssist.config.getFlyExp)) {
            player.sendMessage(ChatColor.RED + "fly効果の発動に必要な経験値が不足しているため、")
            player.sendMessage(ChatColor.RED + "fly効果を終了しました")
            playerdata.flytime = 0
            playerdata.flyflag = false
            player.setAllowFlight(false)
            player.setFlying(false)
          } else {
            player.setAllowFlight(true)
            player.sendMessage(ChatColor.GREEN + "fly効果はあと" + flytime + "分です")
            playerdata.flytime -= 1
            expman.changeExp(minus)
          }
        }
      }
    }
  }
}