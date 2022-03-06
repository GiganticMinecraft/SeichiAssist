package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions

import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize

trait InteractInventory[F[_], Player, Inventory] {

  /**
   * ポケットインベントリをプレーヤーに開かせる作用。
   */
  def open(inventory: Inventory)(player: Player): F[Unit]

  /**
   * ポケットインベントリを最低[[PocketSize]]にまで拡張した結果として得られる新しいインベントリを作成する作用。
   *
   * この作用は、以下の二つのうちどちらかを行う：
   *   - `inventory` 自体のサイズを拡張し、 `inventory` を結果として返す
   *
   * または、マインクラフトのメインスレッド上にて
   *
   *   - `inventory` を開いているすべてのプレーヤーにインベントリを閉じさせる
   *   - 新しいインベントリ `i` を作成し、 `inventory` から `i` にアイテムをすべて移し替え、 `i` を結果として返す
   *
   * よって、プレーヤーにインベントリを紐づけ、サイズを拡張する時に、 この作用により得たインベントリをすぐに紐づけなおすという操作をすることで
   * 紐づいていたインベントリをプレーヤーが開いていたとしてもアイテムの増殖や喪失が起こることは無い。
   */
  def extendSize(newSize: PocketSize)(inventory: Inventory): F[Inventory]

}

object InteractInventory {

  def apply[F[_], P, I](implicit ev: InteractInventory[F, P, I]): InteractInventory[F, P, I] =
    ev

}
