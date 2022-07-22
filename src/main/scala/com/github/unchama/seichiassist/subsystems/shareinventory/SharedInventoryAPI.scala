package com.github.unchama.seichiassist.subsystems.shareinventory

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.SharedFlag
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents

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

  protected val sharedFlagRepository: KeyedDataRepository[Player, Ref[F, SharedFlag]]

  final def sharedFlag(player: Player): F[SharedFlag] = sharedFlagRepository(player).get

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
