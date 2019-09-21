package com.github.unchama.seichiassist.util.ops

import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import scala.collection.JavaConversions._

class ItemStackOps {
  implicit class ItemStackOps(val itemStack: ItemStack) {
    /**
     * [ItemStack] の説明文を表すプロパティ.
     *
     * getter は説明文のコピーを返し,
     * setterはこの[ItemStack]の説明文を与えられた[String]の[List]に書き換える.
     */
    def lore: List[String] = {
      itemStack.getItemMeta.getLore
    }
    def lore_=(value: List[String]): Unit = {
      itemStack.getItemMeta.setLore(value)
    }

    def appendOwnerInformation(owner: Player) {
      lore =
        (if (itemStack.getItemMeta.hasLore) this.lore else Nil) +:
          s"$RESET${DARK_GREEN}所有者：${owner.getName}"
    }

  }
}
