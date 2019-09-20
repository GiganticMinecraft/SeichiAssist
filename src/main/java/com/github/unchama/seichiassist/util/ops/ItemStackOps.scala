package com.github.unchama.seichiassist.util.ops

import org.bukkit.ChatColor._
object ItemStackOps {
  /**
   * [ItemStack] の説明文を表すプロパティ.
   *
   * getter は説明文のコピーを返し,
   * setterはこの[ItemStack]の説明文を与えられた[String]の[List]に書き換える.
   */
  var ItemStack.lore: List[String]
  get() = itemMeta.lore.toList()
  set(value) {
    itemMeta = itemMeta.apply {
      lore = value
    }
  }

  def ItemStack.appendOwnerInformation(owner: Player) {
    itemMeta = itemMeta.apply {
      lore =
        (if (this.hasLore()) this.lore else ArrayList()) +
          s"$RESET${DARK_GREEN}所有者：${owner.name}"
    }
  }
}
