package com.github.unchama.util

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * 主にチェスト用。
 */
inline val Inventory.row: Int
  get() = size / 9

inline fun newChestInventory(owner: InventoryHolder? = null, row: Int = 4, title: String? = null): Inventory {
  return if (title == null) {
    Bukkit.createInventory(owner, row * 9)
  } else {
    Bukkit.createInventory(owner, row * 9, title)
  }
}