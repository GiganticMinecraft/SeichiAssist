package com.github.unchama.seichiassist.util

import org.bukkit.inventory.ItemStack

/**
 * Created by karayuu on 2019/06/08
 */

/**
 * [ItemStack] のLoreを設定します.
 */
fun ItemStack.setLore(lore: List<String>) {
    val itemmeta = this.itemMeta.apply {
        this.lore = lore
    }
}
