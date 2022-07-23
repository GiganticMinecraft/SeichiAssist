package com.github.unchama.seichiassist.subsystems.sharedinventory

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.SharedFlag
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait SharedInventoryWriteAPI[F[_], Player] {

  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit]

  def clear(targetUuid: UUID): F[Unit]

  def setSharing(player: Player): F[Unit]

  def setNotSharing(player: Player): F[Unit]

}

object SharedInventoryWriteAPI {

  def apply[F[_], Player](
    implicit ev: SharedInventoryWriteAPI[F, Player]
  ): SharedInventoryWriteAPI[F, Player] = ev

}

trait SharedInventoryReadAPI[F[_], Player] {

  protected val inventoryContentsRepository: KeyedDataRepository[
    Player,
    Ref[F, InventoryContents]
  ]

  def sharedFlag(player: Player): F[SharedFlag]

  def load(targetUuid: UUID): F[Option[InventoryContents]]

}

object SharedInventoryReadAPI {

  def apply[F[_], Player](
    implicit ev: SharedInventoryReadAPI[F, Player]
  ): SharedInventoryReadAPI[F, Player] = ev

}

trait SharedInventoryAPI[F[_], Player]
    extends SharedInventoryReadAPI[F, Player]
    with SharedInventoryWriteAPI[F, Player]
