package com.github.unchama.seichiassist.subsystems.sharedinventory.domain

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait SharedInventoryPersistence[F[_]] extends RefDict[F, UUID, InventoryContents] {

  /**
   * セーブされている[[InventoryContents]]を完全に削除します。
   */
  def clear(targetUuid: UUID): F[Unit]

}
