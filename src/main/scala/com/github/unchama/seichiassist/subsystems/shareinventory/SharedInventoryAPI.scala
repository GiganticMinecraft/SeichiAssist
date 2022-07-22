package com.github.unchama.seichiassist.subsystems.shareinventory

import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait SharedInventoryWriteAPI[F[_]] {

  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit]

  def clear(targetUuid: UUID): F[Unit]

}

object SharedInventoryWriteAPI {

  def apply[F[_]](implicit ev: SharedInventoryWriteAPI[F]): SharedInventoryWriteAPI[F] = ev

}

trait SharedInventoryReadAPI[F[_]] {

  def load(targetUuid: UUID): F[Option[InventoryContents]]

}

object SharedInventoryReadAPI {

  def apply[F[_]](implicit ev: SharedInventoryReadAPI[F]): SharedInventoryReadAPI[F] = ev

}

trait SharedInventoryAPI[F[_]] extends SharedInventoryReadAPI[F] with SharedInventoryWriteAPI[F]
