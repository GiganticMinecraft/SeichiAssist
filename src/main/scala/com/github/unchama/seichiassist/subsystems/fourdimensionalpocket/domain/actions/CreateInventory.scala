package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions

import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize

trait CreateInventory[F[_], Inventory] {

  /**
   * ポケットインベントリを新しく作成する作用。
   */
  def create(size: PocketSize): F[Inventory]

}

object CreateInventory {

  def apply[F[_], Inventory](
    implicit ev: CreateInventory[F, Inventory]
  ): CreateInventory[F, Inventory] = ev

}
