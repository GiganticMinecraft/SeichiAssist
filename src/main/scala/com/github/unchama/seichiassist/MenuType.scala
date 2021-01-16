package com.github.unchama.seichiassist

import org.bukkit.ChatColor.{BOLD, DARK_PURPLE}


sealed trait MenuType {
  def invName: String
}

object MenuType {
  case object HEAD extends MenuType {
    override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「前」"
  }

  case object MIDDLE extends MenuType {
    override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「中」"
  }

  case object TAIL extends MenuType {
    override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「後」"
  }

  case object SHOP extends MenuType {
    override def invName: String = s"$DARK_PURPLE${BOLD}実績ポイントショップ"
  }

  case object COMBINE extends MenuType {
    override def invName: String = s"$DARK_PURPLE${BOLD}二つ名組合せシステム"
  }
}
