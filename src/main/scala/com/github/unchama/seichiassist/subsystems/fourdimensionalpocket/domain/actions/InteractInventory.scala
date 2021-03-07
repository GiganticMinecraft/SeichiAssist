package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions

import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize

trait InteractInventory[F[_], Player, Inventory] {

  /**
   * ポケットインベントリをプレーヤーに開かせる作用。
   */
  def open(inventory: Inventory)(player: Player): F[Unit]

  /**
   * ポケットインベントリを最低[[PocketSize]]にまで拡張した結果の新しいインベントリを作成する作用。
   */
  def extendSize(newSize: PocketSize)(inventory: Inventory): F[Inventory]

}

object InteractInventory {

  def apply[F[_], P, I](implicit ev: InteractInventory[F, P, I]): InteractInventory[F, P, I] = ev

}
