package com.github.unchama.seichiassist.subsystems.sharedinventory.domain

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait SharedInventoryPersistence[F[_]] extends RefDict[F, UUID, InventoryContents] {

  /**
   * [[InventoryContents]]をセーブします。
   * @param inventoryContents セーブ対象の[[InventoryContents]]
   */
  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit]

  /**
   * セーブされている[[InventoryContents]]をロードします。
   */
  def load(targetUuid: UUID): F[Option[InventoryContents]]

  /**
   * セーブされている[[InventoryContents]]を完全に削除します。
   */
  def clear(targetUuid: UUID): F[Unit]

}
