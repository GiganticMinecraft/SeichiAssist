package com.github.unchama.seichiassist.subsystems.shareinventory.domain

import org.bukkit.inventory.ItemStack

case class InventoryContents(inventoryContents: List[ItemStack]) {
  require(inventoryContents.nonEmpty)
}
