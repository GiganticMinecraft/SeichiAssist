package com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import cats.effect.IO
import org.bukkit.entity.Player

object BukkitGachaSkullData {

  /**
   * ノーマルガチャ券
   */
  def gachaSkull(implicit playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]): ItemStack =
    new SkullItemStackBuilder(SkullOwners.unchama)
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(List(s"$RESET${GREEN}右クリックで使えます"))
      .build()

  /**
   * 投票報酬のガチャ券
   */
  def gachaForVoting(implicit playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]): ItemStack =
    new SkullItemStackBuilder(SkullOwners.unchama)
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${LIGHT_PURPLE}投票ありがとナス♡"))
      .build()

  /**
   * ガチャ景品（当たり・大当たり）とガチャ券の交換システムで手に入るガチャ券
   */
  def gachaForExchanging(implicit playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]): ItemStack =
    new SkullItemStackBuilder(SkullOwners.unchama)
      .title(s"$YELLOW${BOLD}ガチャ券")
      .lore(List(s"$RESET${GREEN}右クリックで使えます", s"$RESET${GRAY}ガチャ景品と交換しました。"))
      .build()
}
