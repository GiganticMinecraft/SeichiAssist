package com.github.unchama.seichiassist.util.ops

import org.bukkit.ChatColor.DARK_GREEN
import org.bukkit.ChatColor.RESET
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Created by karayuu on 2019/06/08
 */

/**
 * [ItemStack] の説明文を表すプロパティ.
 *
 * getter は説明文のコピーを返し,
 * setterはこの[ItemStack]の説明文を与えられた[String]の[List]に書き換える.
 */
var ItemStack.lore: List<String>
  get() = itemMeta.lore.toList()
  set(value) {
    itemMeta = itemMeta.apply {
      lore = value
    }
  }

fun ItemStack.appendOwnerInformation(owner: Player) {
  itemMeta = itemMeta.apply {
    lore =
        (if (this.hasLore()) this.lore else ArrayList()) +
            "$RESET${DARK_GREEN}所有者：${owner.name}"
  }
}
