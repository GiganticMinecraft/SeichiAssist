package com.github.unchama.seasonalevents.anniversary

import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerTextureValue}
import com.github.unchama.seasonalevents.anniversary.Anniversary.ANNIVERSARY_COUNT
import org.bukkit.ChatColor.YELLOW
import org.bukkit.inventory.ItemStack

object AnniversaryItemData {
  private val mineChan = SkullOwnerTextureValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDhmNTQ0OGI0ZDg4ZTQwYjE0YzgyOGM2ZjFiNTliMzg1NDVkZGE5MzNlNzNkZmYzZjY5NWU2ZmI0Mjc4MSJ9fX0=")

  val mineHead: ItemStack = new SkullItemStackBuilder(mineChan)
    .title("まいんちゃん")
    .lore(List(
      "",
      s"${YELLOW}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年記念だよ！"
    ))
    .build()
}