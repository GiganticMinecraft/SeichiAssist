package com.github.unchama.seichiassist.task.cooldown

import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

final class SharedInventoryCoolDownResetTask(player: Player) extends BukkitRunnable {
  private val pd = SeichiAssist.playermap(player)

  override def run(): Unit = {
    pd.shareinvcooldownflag = false
  }
}
