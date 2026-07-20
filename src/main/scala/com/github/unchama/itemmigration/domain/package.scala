package com.github.unchama.itemmigration

import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.numeric.GreaterEqual
import org.bukkit.inventory.ItemStack

package object domain {
  type ItemMigrationVersionComponent = Int :| GreaterEqual[0]

  type ItemStackConversion = ItemStack => ItemStack
}
