package com.github.unchama.seichiassist.subsystems.shareinventory

import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait ShareInventoryWriteAPI[F[_]] {

  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit]

  def clear(targetUuid: UUID): F[Unit]

}

object ShareInventoryWriteAPI {

  def apply[F[_]](implicit ev: ShareInventoryWriteAPI[F]): ShareInventoryWriteAPI[F] = ev

}

trait ShareInventoryReadAPI[F[_]] {

  def load(targetUuid: UUID): F[InventoryContents]

}

object ShareInventoryReadAPI {

  def apply[F[_]](implicit ev: ShareInventoryReadAPI[F]): ShareInventoryReadAPI[F] = ev

}

trait ShareInventoryAPI[F[_]] extends ShareInventoryReadAPI[F] with ShareInventoryWriteAPI[F]
