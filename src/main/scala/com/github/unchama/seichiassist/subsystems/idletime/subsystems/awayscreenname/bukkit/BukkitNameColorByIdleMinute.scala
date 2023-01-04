package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.bukkit

import com.github.unchama.seichiassist.subsystems.idletime.domain.IdleMinute
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain.NameColorByIdleMinute
import org.bukkit.ChatColor

object BukkitNameColorByIdleMinute extends NameColorByIdleMinute[ChatColor] {

  override def getNameColor(idleMinute: IdleMinute): ChatColor = {
    if (idleMinute.minutes >= 10) ChatColor.DARK_GRAY
    else if (idleMinute.minutes >= 3) ChatColor.GRAY
    else ChatColor.RESET
  }

}
