package com.github.unchama.seichiassist.subsystems.sharedinventory.domain

import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait SharedInventoryPersistence[F[_]] {

  /**
   * セーブされている[[InventoryContents]]を完全に削除します。
   */
  def clear(targetUuid: UUID): F[Unit]

  /**
   * セーブされている[[InventoryContents]]を読み込みます
   */
  def load(targetUuid: UUID): F[Option[InventoryContents]]

  /**
   * [[InventoryContents]]をセーブします。
   */
  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit]

}
