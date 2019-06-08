package com.github.unchama.seichiassist.util

import org.bukkit.inventory.ItemStack

/**
 * Created by karayuu on 2019/06/08
 */

/**
 * [ItemStack] のLoreを設定します. ただし, [List] に ``null`` が入っている場合は無視されます.
 */
fun ItemStack.setLoreNotNull(lore: List<String?>) {
    this.itemMeta = this.itemMeta.apply {
        this.lore = (lore.filterNotNull())
    }
}
