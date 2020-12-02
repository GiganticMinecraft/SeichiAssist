package com.github.unchama.seasonalevents.anniversary

import com.github.unchama.seasonalevents.SkullData
import com.github.unchama.seasonalevents.Util.createCustomHead
import com.github.unchama.seasonalevents.anniversary.Anniversary.ANNIVERSARY_COUNT
import org.bukkit.ChatColor.YELLOW
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object AnniversaryItemData {
  val mineHead: Option[ItemStack] = createCustomHead(SkullData.MineChan).map { item =>
    val loreList = List(
      "",
      s"${YELLOW}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年記念だよ！"
    ).asJava

    val itemMeta = item.getItemMeta.tap { meta =>
      import meta._
      setDisplayName("まいんちゃん")
      setLore(loreList)
    }

    item.setItemMeta(itemMeta)
    item
  }
}