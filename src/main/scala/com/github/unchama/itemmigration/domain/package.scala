package com.github.unchama.itemmigration

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import org.bukkit.inventory.ItemStack

package object domain {
  type VersionComponent = Int Refined NonNegative

  type VersionNumber = IndexedSeq[VersionComponent]

  type MigrationSequence = IndexedSeq[ItemMigration]

  type ItemStackConversion = ItemStack => ItemStack
}
