package com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit

import org.bukkit.inventory.ItemStack

case class InventoryContents private (inventoryContents: List[ItemStack])

object InventoryContents {
  val initial: InventoryContents = InventoryContents(List.empty)

  def ofNonEmpty(inventoryContents: List[ItemStack]): InventoryContents = {
    require(inventoryContents.nonEmpty)
    InventoryContents(inventoryContents)
  }
}
