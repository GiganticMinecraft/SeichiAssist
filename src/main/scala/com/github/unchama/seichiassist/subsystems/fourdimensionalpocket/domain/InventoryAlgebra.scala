package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain

trait InventoryAlgebra[F[_], Player, Inventory] {

  /**
   * インベントリをプレーヤーに開かせる作用。
   */
  def open(inventory: Inventory)(player: Player): F[Unit]

  /**
   * インベントリを最低[[PocketSize]]にまで拡張した結果の新しいインベントリを作成する作用。
   */
  def extendSize(newSize: PocketSize)(inventory: Inventory): F[Inventory]

}
