package com.github.unchama.seichiassist

import org.bukkit.ChatColor.{BOLD, DARK_PURPLE}


sealed trait MenuType {
  val inventoryTitle: String
}

object MenuType {
  case object HEAD extends MenuType {
    override final val inventoryTitle: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「前」"
  }

  case object MIDDLE extends MenuType {
    override final val inventoryTitle: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「中」"
  }

  case object TAIL extends MenuType {
    override final val inventoryTitle: String = s"$DARK_PURPLE${BOLD}二つ名組合せ「後」"
  }

  case object SHOP extends MenuType {
    override final val inventoryTitle: String = s"$DARK_PURPLE${BOLD}実績ポイントショップ"
  }

  case object COMBINE extends MenuType {
    override final val inventoryTitle: String = s"$DARK_PURPLE${BOLD}二つ名組合せシステム"
  }
}
