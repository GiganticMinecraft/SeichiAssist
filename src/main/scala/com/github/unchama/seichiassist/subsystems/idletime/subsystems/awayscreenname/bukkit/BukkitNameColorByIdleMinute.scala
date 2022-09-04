package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.bukkit

import com.github.unchama.seichiassist.subsystems.idletime.domain.IdleMinute
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain.NameColorByIdleMinute
import org.bukkit.ChatColor

object BukkitNameColorByIdleMinute extends NameColorByIdleMinute[ChatColor] {

  override def getNameColor(idleMinute: IdleMinute): ChatColor = {
    if (idleMinute.minute >= 10) ChatColor.DARK_GRAY
    else if (idleMinute.minute >= 3) ChatColor.GRAY
    else ChatColor.RESET
  }

}
