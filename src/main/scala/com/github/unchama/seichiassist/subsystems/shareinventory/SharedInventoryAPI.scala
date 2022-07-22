package com.github.unchama.seichiassist.subsystems.shareinventory

import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.SharedFlag
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait SharedInventoryWriteAPI[F[_]] {

  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit]

  def clear(targetUuid: UUID): F[Unit]

}

object SharedInventoryWriteAPI {

  def apply[F[_]](implicit ev: SharedInventoryWriteAPI[F]): SharedInventoryWriteAPI[F] = ev

}

trait SharedInventoryReadAPI[F[_], Player] {

  val sharedFlag: KeyedDataRepository[Player, ReadOnlyRef[F, SharedFlag]]

  def load(targetUuid: UUID): F[Option[InventoryContents]]

}

object SharedInventoryReadAPI {

  def apply[F[_], Player](
    implicit ev: SharedInventoryReadAPI[F, Player]
  ): SharedInventoryReadAPI[F, Player] = ev

}

trait SharedInventoryAPI[F[_], Player]
    extends SharedInventoryReadAPI[F, Player]
    with SharedInventoryWriteAPI[F]
