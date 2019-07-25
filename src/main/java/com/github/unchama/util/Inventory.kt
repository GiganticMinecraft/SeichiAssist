package com.github.unchama.util

import org.bukkit.inventory.Inventory

/**
 * 主にチェスト用。
 */
inline val Inventory.row: Int
  get() = size / 9