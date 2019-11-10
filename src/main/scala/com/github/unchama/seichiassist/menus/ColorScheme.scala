package com.github.unchama.seichiassist.menus

import org.bukkit.ChatColor._

object ColorScheme {
  type Scheme = String => String

  val navigation: Scheme = s"$YELLOW$UNDERLINE$BOLD" + _

  val clickResultDescription: Scheme = s"$RESET$DARK_RED$UNDERLINE" + _
}
