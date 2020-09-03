package com.github.unchama.itemmigration.util

import com.github.unchama.itemmigration.domain.ItemStackConversion
import org.bukkit.Material
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.{Inventory, InventoryHolder}

object MigrationHelper {

  /**
   * `inventory` に含まれるすべてのアイテムスタックを `conversion` にて破壊的に変換する。
   */
  def convertEachStackIn(inventory: Inventory)(conversion: ItemStackConversion): Unit = {
    for (index <- 0 until inventory.getSize) {
      val item = inventory.getItem(index)
      if (item != null && item.getType != Material.AIR) {
        inventory.setItem(index, conversion(item))
      }
    }
  }

  /**
   * シュルカーボックス等の内部にインベントリを持つアイテムについて、
   * インベントリ内部のアイテムに対して変換を行うように `conversion` をラップする。
   */
  def delegateConversionForContainers(conversion: ItemStackConversion): ItemStackConversion = {
    itemStack => {
      val cloned = itemStack.clone()
      cloned.getItemMeta match {
        case meta: BlockStateMeta if meta.hasBlockState =>
          meta.getBlockState match {
            case state: InventoryHolder =>
              convertEachStackIn(state.getInventory)(conversion)
              cloned
            case _ => conversion(itemStack)
          }
        case _ => conversion(itemStack)
      }
    }
  }

}
