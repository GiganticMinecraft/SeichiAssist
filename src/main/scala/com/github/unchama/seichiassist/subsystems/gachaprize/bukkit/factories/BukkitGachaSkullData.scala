package com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seichiassist.SkullOwners
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

object BukkitGachaSkullData {

  /**
   * ノーマルガチャ券
   */
  val gachaSkull: ItemStack =
    new SkullItemStackBuilder(SkullOwners.unchama)
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(List(s"$RESET${GREEN}右クリックで使えます"))
      .build()

  /**
   * 投票報酬のガチャ券
   */
  val gachaForVoting: ItemStack =
    new SkullItemStackBuilder(SkullOwners.unchama)
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${LIGHT_PURPLE}投票ありがとナス♡"))
      .build()

  /**
   * ガチャ景品（当たり・大当たり）とガチャ券の交換システムで手に入るガチャ券
   */
  val gachaForExchanging: ItemStack =
    new SkullItemStackBuilder(SkullOwners.unchama)
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${GRAY}ガチャ景品と交換しました。"))
      .build()
}
