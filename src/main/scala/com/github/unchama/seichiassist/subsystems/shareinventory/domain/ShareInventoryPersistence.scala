package com.github.unchama.seichiassist.subsystems.shareinventory.domain

trait ShareInventoryPersistence[F[_]] {

  /**
   * [[InventoryContents]]をセーブします。
   * @param inventoryContents セーブ対象の[[InventoryContents]]
   */
  def saveSerializedShareInventory(inventoryContents: InventoryContents): F[Unit]

  /**
   * セーブされている[[InventoryContents]]をロードします。
   */
  def loadSerializedShareInventory(): F[InventoryContents]

}
