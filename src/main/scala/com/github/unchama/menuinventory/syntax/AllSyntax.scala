package com.github.unchama.menuinventory.syntax

import com.github.unchama.menuinventory.InventoryRowSize
import com.github.unchama.menuinventory.InventoryRowSize.InventorySize

trait InventorySizeIntOps {
  implicit final class IntInventorySizeOps(val rowNumber: Int) {
    def rows: InventorySize = Left(InventoryRowSize(rowNumber))
  }
}

trait AllSyntax extends InventorySizeIntOps
