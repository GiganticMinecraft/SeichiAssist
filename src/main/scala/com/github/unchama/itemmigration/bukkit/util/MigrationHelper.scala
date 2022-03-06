package com.github.unchama.itemmigration.bukkit.util

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
   * シュルカーボックス等の内部にインベントリを持つアイテムについて、 インベントリ内部のアイテムに対して変換を行うように `conversion` をラップする。
   */
  def delegateConversionForContainers(conversion: ItemStackConversion): ItemStackConversion = {
    itemStack =>
      {
        val cloned = itemStack.clone()
        cloned.getItemMeta match {
          case meta: BlockStateMeta if meta.hasBlockState =>
            meta.getBlockState match {
              case state: InventoryHolder =>
                /**
                 * [このスレッド](https://www.spigotmc.org/threads/unable-to-modify-shulker-box-inventory-contents.320665/)
                 * を参考に実装。
                 *
                 * [[BlockStateMeta]] の `.getBlockState` メソッドは
                 *
                 * > The state is a copy, it must be set back (or to another item) with
                 * setBlockState(BlockState)
                 *
                 * にあるように状態のコピーを返してくるので、それに付属したインベントリ(`state.getInventory`) に変更を行った後は `state` を
                 * `meta` に返して、 `meta` を `cloned` に返す必要がある。
                 */
                convertEachStackIn(state.getInventory)(conversion)
                meta.setBlockState(state)
                cloned.setItemMeta(meta)
                cloned
              case _ => conversion(itemStack)
            }
          case _ => conversion(itemStack)
        }
      }
  }

}
