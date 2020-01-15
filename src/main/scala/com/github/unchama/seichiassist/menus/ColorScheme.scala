package com.github.unchama.seichiassist.menus

import org.bukkit.ChatColor._

object ColorScheme {
  type Scheme = String => String

  val navigation: Scheme = s"$RESET$YELLOW$UNDERLINE$BOLD" + _

  val purpleBold: Scheme = s"$RESET$DARK_PURPLE$BOLD" + _

  val clickResultDescription: Scheme = s"$RESET$DARK_RED$UNDERLINE" + _
}
