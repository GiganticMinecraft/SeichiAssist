package com.github.unchama.itemmigration

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import org.bukkit.inventory.ItemStack

package object domain {
  type ItemMigrationVersionComponent = Int Refined NonNegative

  type ItemStackConversion = ItemStack => ItemStack
}
