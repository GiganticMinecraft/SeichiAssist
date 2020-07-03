package com.github.unchama.itemmigration

import com.github.unchama.itemmigration.ItemMigration.ItemConversion
import org.bukkit.Material
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.{Inventory, InventoryHolder}

object MigrationHelper {
  def convertEachStackIn(inventory: Inventory)(conversion: ItemConversion): Unit = {
    for (index <- 0 until inventory.getSize) {
      val item = inventory.getItem(index)
      if (item != null && item.getType != Material.AIR) {
        inventory.setItem(index, conversion(item))
      }
    }
  }

  def delegateConversionForContainers(conversion: ItemConversion): ItemConversion = {
    itemStack => {
      itemStack.getItemMeta match {
        case meta: BlockStateMeta if meta.hasBlockState =>
          meta.getBlockState match {
            case state: InventoryHolder =>
              convertEachStackIn(state.getInventory)(conversion)
              itemStack
            case _ => conversion(itemStack)
          }
        case _ => conversion(itemStack)
      }
    }
  }

}
