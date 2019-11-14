package com.github.unchama.menuinventory.syntax

import com.github.unchama.menuinventory.InventoryRowSize
import com.github.unchama.menuinventory.InventoryRowSize.InventorySize

trait InventorySizeIntOps {
  implicit final class IntInventorySizeOps(val rowNumber: Int) {
    def chestRows: InventorySize = Left(InventoryRowSize(rowNumber))
  }
}

trait InventorySizeOps {
  implicit final class InventorySizeOps(val size: InventorySize) {
    def slotCount: Int =
      size match {
        case Left(rowSize) => rowSize.rows * 9
        case Right(inventoryType) => inventoryType.getDefaultSize
      }
  }
}

trait AllSyntax extends InventorySizeIntOps
  with InventorySizeOps
