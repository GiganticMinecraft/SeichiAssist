package com.github.unchama.seichiassist.subsystems.shareinventory.domain

import java.util.UUID

trait ShareInventoryPersistence[F[_]] {

  /**
   * [[InventoryContents]]をセーブします。
   * @param inventoryContents セーブ対象の[[InventoryContents]]
   */
  def saveSerializedShareInventory(
    targetUuid: UUID,
    inventoryContents: InventoryContents
  ): F[Unit]

  /**
   * セーブされている[[InventoryContents]]をロードします。
   */
  def loadSerializedShareInventory(targetUuid: UUID): F[InventoryContents]

}
