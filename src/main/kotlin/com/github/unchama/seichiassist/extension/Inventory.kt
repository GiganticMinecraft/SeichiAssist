package com.github.unchama.seichiassist.extension

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * @author tar0ss
 * @author unchama
 */
fun Inventory.setItemAsync(slot: Int, itemStack: ItemStack?) {
    runTaskAsync { setItem(slot, itemStack) }
}