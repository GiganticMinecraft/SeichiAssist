package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain

trait PocketInventoryAlgebra[F[_], Player, Inventory] {

  /**
   * ポケットインベントリをプレーヤーに開かせる作用。
   */
  def open(inventory: Inventory)(player: Player): F[Unit]

  /**
   * ポケットインベントリを新しく作成する作用。
   */
  def create(size: PocketSize): F[Inventory]

  /**
   * ポケットインベントリを最低[[PocketSize]]にまで拡張した結果の新しいインベントリを作成する作用。
   */
  def extendSize(newSize: PocketSize)(inventory: Inventory): F[Inventory]

}

object PocketInventoryAlgebra {

  def apply[F[_], P, I](implicit ev: PocketInventoryAlgebra[F, P, I]): PocketInventoryAlgebra[F, P, I] = ev

}
