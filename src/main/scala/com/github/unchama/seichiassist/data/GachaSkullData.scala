package com.github.unchama.seichiassist.data

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

object GachaSkullData {
  /**
   * ノーマルガチャ券
   */
  val gachaSkull: ItemStack =
    new SkullItemStackBuilder("unchama")
      .title("$YELLOW${BOLD}ガチャ券")
      .lore(
        s"$RESET${GREEN}右クリックで使えます"
      )
      .build()

  /**
   * お詫びガチャ券
   */
  val gachaFromAdministrator: ItemStack =
    new SkullItemStackBuilder("unchama")
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(
        s"$RESET${GREEN}右クリックで使えます",
        s"$RESET${DARK_RED}運営から不具合のお詫びです"
      )
      .build()

  /**
   * 投票報酬のガチャ券
   */
  val gachaForVoting: ItemStack =
    new SkullItemStackBuilder("unchama")
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(
        s"$RESET${GREEN}右クリックで使えます",
        s"$RESET${LIGHT_PURPLE}投票ありがとナス♡"
      )
      .build()

  /**
   * ガチャ景品（当たり・大当たり）とガチャ券の交換システムで手に入るガチャ券
   */
  val gachaForExchanging: ItemStack =
    new SkullItemStackBuilder("unchama")
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(
        s"$RESET${GREEN}右クリックで使えます",
        s"$RESET${GRAY}ガチャ景品と交換しました。"
      )
      .build()

  /**
   * 整地レベルアップ時に配布されるガチャ券
   */
  val gachaForSeichiLevelUp: ItemStack =
    new SkullItemStackBuilder("unchama")
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(
        s"$RESET${GREEN}右クリックで使えます",
        s"$RESET${GRAY}レベルアップ記念です"
      )
      .build()
}
