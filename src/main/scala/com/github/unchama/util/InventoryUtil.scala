package com.github.unchama.util

import com.github.unchama.menuinventory.InventoryRowSize
import com.github.unchama.menuinventory.InventoryRowSize.InventorySize
import org.bukkit.Bukkit
import org.bukkit.inventory.{Inventory, InventoryHolder}

object InventoryUtil {

  implicit class InventoryOps(val inventory: Inventory) extends AnyVal {
    def row: Int = inventory.getSize / 9
  }

  def createInventory(holder: Option[InventoryHolder] = None,
                      size: InventorySize = Left(InventoryRowSize(4)),
                      title: Option[String] = None): Inventory =
    size match {
      case Left(size) => Bukkit.createInventory(holder.orNull, size.rows * 9, title.orNull)
      case Right(size) => Bukkit.createInventory(holder.orNull, size, title.orNull)
    }
}
