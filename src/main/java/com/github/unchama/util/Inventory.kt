package com.github.unchama.util

import arrow.core.Either
import com.github.unchama.menuinventory.InventorySize
import com.github.unchama.menuinventory.rows
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * 主にチェスト用。
 */
inline val Inventory.row: Int
  get() = size / 9

fun createInventory(holder: InventoryHolder? = null,
                    size: InventorySize = 4.rows(),
                    title: String? = null): Inventory =
    when (size) {
      is Either.Left -> Bukkit.createInventory(holder, size.a.rows * 9, title)
      is Either.Right -> Bukkit.createInventory(holder, size.b, title)
    }
