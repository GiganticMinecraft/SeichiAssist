package com.github.unchama.seasonalevents.anniversary

import java.util.UUID

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seasonalevents.SkullData
import com.github.unchama.seasonalevents.anniversary.Anniversary.ANNIVERSARY_COUNT
import org.bukkit.ChatColor.YELLOW
import org.bukkit.inventory.ItemStack

object AnniversaryItemData {

  val mineHead: ItemStack = new SkullItemStackBuilder(UUID.randomUUID(), SkullData.MineChan.textureValue)
    .title("まいんちゃん")
    .lore(List(
      "",
      s"${YELLOW}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年記念だよ！"
    ))
    .build()
}