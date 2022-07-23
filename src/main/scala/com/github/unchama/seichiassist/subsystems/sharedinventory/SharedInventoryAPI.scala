package com.github.unchama.seichiassist.subsystems.sharedinventory

import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.SharedFlag
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents

import java.util.UUID

trait SharedInventoryWriteAPI[F[_], Player] {

  def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit]

  def clear(targetUuid: UUID): F[Unit]

}

object SharedInventoryWriteAPI {

  def apply[F[_], Player](
    implicit ev: SharedInventoryWriteAPI[F, Player]
  ): SharedInventoryWriteAPI[F, Player] = ev

}

trait SharedInventoryReadAPI[F[_], Player] {

  /**
   * 現在のインベントリ格納状況を確認します。
   * NOTE: このFlagは実際の格納状況に依存します。
   */
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
